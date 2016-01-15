package nu.nldv.unroar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
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
import nu.nldv.unroar.model.GuessType;
import nu.nldv.unroar.model.QueueItem;
import nu.nldv.unroar.model.RarArchiveFolder;
import nu.nldv.unroar.model.UnrarResponseObject;
import nu.nldv.unroar.model.UnrarStatus;

@Controller
@SpringBootApplication
@Import(UppackarenConfig.class)
public class MainController {

    private final Logger logger;

    @Autowired
    private DirectoriesOnlyFilter directoriesOnlyFilter;
    @Autowired
    private RarFileFilter rarFileFilter;
    @Autowired
    private NoHiddenFilesFilter noHiddenFilesFilter;
    @Autowired
    private Unrarer unrarer;

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

        String queueId = unrarer.addFileToUnrarQueue(dir);
        return new ResponseEntity<>(new UnrarResponseObject(queueId), HttpStatus.OK);
    }

    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    public ResponseEntity<List<QueueItem>> getQueue() {
        return new ResponseEntity<List<QueueItem>>(new ArrayList<>(unrarer.getQueue()), HttpStatus.OK);
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity getUnpackingStatus() throws IOException {
        final File currentWorkFile = unrarer.getCurrentWork();
        if (currentWorkFile == null) {
            return ResponseEntity.notFound().build();
        } 
        final String filePath = unrarer.guessFile(currentWorkFile, GuessType.PATH);
        final String guessedFileName = unrarer.guessFile(currentWorkFile, GuessType.NAME);
        logger.debug("status - filePath = "+filePath);
        logger.debug("status - guessedFileName = "+guessedFileName);
        final File newFile = new File(filePath);
        if (newFile.exists()) {
            logger.debug("status - newFile.exists = "+newFile.exists());
            final int currentSizeOfFile = RarArchiveFolder.calculateDirSize(new File[]{newFile.getParentFile()});
            final float percentDone = (float) currentSizeOfFile / (float) RarArchiveFolder.calculateDirSize(currentWorkFile.listFiles());
            return new ResponseEntity<>(new UnrarStatus(guessedFileName, (int) (percentDone * 100)), HttpStatus.OK);
        } else if((new File(newFile.getParentFile().getParent()+File.separator+guessedFileName).exists())) {
            logger.debug("inside the else if");
            final File file = new File(newFile.getParentFile().getParent() + File.separator + guessedFileName);
            logger.debug("status - file.exists = "+file.exists());
            final int currentSizeOfFile = RarArchiveFolder.calculateDirSize(new File[]{file});
            final float percentDone = (float) currentSizeOfFile / (float) RarArchiveFolder.calculateDirSize(currentWorkFile.listFiles());
            return new ResponseEntity<>(new UnrarStatus(guessedFileName, (int) (percentDone * 100)), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
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
                if (RarArchiveFolder.constructIdFromFile(dir).equalsIgnoreCase(id)) {
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

    public MainController() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
}