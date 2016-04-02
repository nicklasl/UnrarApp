package nu.nldv.unroar;

import nu.nldv.unroar.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.nldv.unroar.filter.DirectoriesOnlyFilter;
import nu.nldv.unroar.filter.NoHiddenFilesFilter;
import nu.nldv.unroar.filter.RarFileFilter;
import nu.nldv.unroar.util.FileUtils;

@Controller
@SpringBootApplication
@Import(UppackarenConfig.class)
public class MainController {

    private static final Logger logger;

    @Autowired
    private DirectoriesOnlyFilter directoriesOnlyFilter;
    @Autowired
    private RarFileFilter rarFileFilter;
    @Autowired
    private NoHiddenFilesFilter noHiddenFilesFilter;
    @Autowired
    private Unrarer unrarer;
    @Autowired
    private TaskExecutor taskExecutor;

    public static String path;


    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public String getInfo() {
        return "System.currentTimeMillis = " + System.currentTimeMillis();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public List<RarArchiveFolder> listRarArchives() throws IOException {
        File root = new File(path);
        List<RarArchiveFolder> archiveFolders = constructListOfArchiveFolders(root);
        return archiveFolders;
    }

    @RequestMapping(value = "/{pathId}", method = RequestMethod.GET)
    @ResponseBody
    public List<RarArchiveFolder> listRarArchives(@PathVariable String pathId) throws IOException {
        File root = new File(path);
        final File currentDir = findFileById(pathId, root);
        List<RarArchiveFolder> archiveFolders = constructListOfArchiveFolders(currentDir);
        return archiveFolders;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<UnrarResponseObject> unRarArchive(@PathVariable final String id) {
        File root = new File(path);
        File dir = findFileById(id, root);
        if (dir == null) {
            return new ResponseEntity<>(new UnrarResponseObject("0"), HttpStatus.NOT_FOUND);
        }

        String queueId = unrarer.addFileToUnrarQueue(dir, new Completion(){
            @Override
            public void success() {
                logger.info("Trying to download subtitles.");
                final String fileName = unrarer.guessFile(dir, GuessType.NAME);
                taskExecutor.execute(new SubtitleDownloader(fileName, output -> logger.info(output)));
            }

            @Override
            public void fail() {
                //Ignore
            }
        });
        return new ResponseEntity<>(new UnrarResponseObject(queueId), HttpStatus.OK);
    }

    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    public ResponseEntity<List<QueueItem>> getQueue() {
        return new ResponseEntity<List<QueueItem>>(new ArrayList<>(unrarer.getQueue()), HttpStatus.OK);
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity getUnpackingStatus() throws IOException {
        final CurrentWorkUnit currentWorkUnit = unrarer.getCurrentWork();
        if (currentWorkUnit == null || !currentWorkUnit.getUnpackedFile().exists()) {
            return ResponseEntity.notFound().build();
        } else {
            final int currentSizeOfFile = FileUtils.calculateFileSize(currentWorkUnit.getUnpackedFile());
            File containingFolder = currentWorkUnit.getArchiveFile().getParentFile();
            final float percentDone = (float) currentSizeOfFile / (float) FileUtils.calculateFileSize(containingFolder.listFiles());
            return new ResponseEntity<>(new UnrarStatus(currentWorkUnit.getUnpackedFile().getName(), (int) (percentDone * 100)), HttpStatus.OK);
        }
    }

    private List<RarArchiveFolder> constructListOfArchiveFolders(File currentDir) {
        List<RarArchiveFolder> archiveFolders = new ArrayList<>();
        File[] files = currentDir.listFiles(noHiddenFilesFilter);
        if (files != null) {
            for (File dir : files) {
                if (!dir.isDirectory()) {
                    continue;
                }
                if (containsRarFiles(dir) && !alreadyUnpacked(dir, files) && !inQueue(dir)) {
                    archiveFolders.add(new RarArchiveFolder(dir, false));
                } else if (hasSubDir(dir)) {
                    archiveFolders.add(new RarArchiveFolder(dir, true));
                }
            }
        }
        return archiveFolders;
    }

    private boolean hasSubDir(File dir) {
        return dir != null
                && Arrays.stream(dir.listFiles(directoriesOnlyFilter)).anyMatch((file) -> containsRarFiles(file));
    }

    private boolean inQueue(File dir) {
        boolean inQueue = false;
        for (QueueItem qi : unrarer.getQueue()) {
            if (qi.getDir().equals(dir)) {
                inQueue = true;
                break;
            }
        }

        return inQueue || (unrarer.getCurrentWork() != null && unrarer.getCurrentWork().equals(dir));
    }


    private boolean alreadyUnpacked(File dir, File[] files) {
        String unpackedFilePath = unrarer.guessFile(dir, GuessType.PATH);
        boolean result = containsFileWithName(files, unpackedFilePath);
        return result;
    }

    private boolean containsFileWithName(File[] files, String unpackedFileName) {
        for (File file : files) {
            if (file.getAbsolutePath().equalsIgnoreCase(unpackedFileName)) {
                return true;
            }
        }
        return false;
    }

    private File findFileById(String id, File root) {
        File result = null;
        File[] files = root.listFiles(directoriesOnlyFilter);
        if (files != null) {
            for (File dir : files) {
                if (FileUtils.constructIdFromFile(dir).equalsIgnoreCase(id)) {
                    result = dir;
                }
                if (result == null && hasSubDir(dir)) {
                    result = findFileById(id, dir);
                }
            }
        }
        return result;
    }


    private boolean containsRarFiles(File dir) {
        if (dir == null) {
            return false;
        } else {
            return Arrays.stream(dir.listFiles(rarFileFilter)).count() > 0;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Need to supply a directory when starting.");
        }
        path = args[0];
        SpringApplication.run(MainController.class, args);
    }

    static {
        logger = LoggerFactory.getLogger(MainController.class);
    }

    public MainController() {

    }
}