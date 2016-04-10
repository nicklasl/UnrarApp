package nu.nldv.unroar;

import nu.nldv.unroar.filter.DirectoriesOnlyFilter;
import nu.nldv.unroar.filter.NoHiddenFilesFilter;
import nu.nldv.unroar.filter.RarFileFilter;
import nu.nldv.unroar.model.*;
import nu.nldv.unroar.util.FileUtils;
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
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
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

    public static String path = Application.path;


    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String getInfo() {
        return "System.currentTimeMillis = " + System.currentTimeMillis();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<RarArchiveFolder> listRarArchives() throws IOException {
        File root = new File(path);
        return constructListOfArchiveFolders(root);
    }

    @RequestMapping(value = "/{pathId}", method = RequestMethod.GET)
    public List<RarArchiveFolder> listRarArchives(@PathVariable String pathId) throws IOException {
        File root = new File(path);
        final File currentDir = findFileById(pathId, root);
        return constructListOfArchiveFolders(currentDir);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<UnrarResponseObject> unRarArchive(@PathVariable final String id,
                                                            @RequestParam(value = "downloadSubs", required = false) final boolean downloadSubs) {
        File root = new File(path);
        final File dir = findFileById(id, root);
        if (dir == null) {
            return new ResponseEntity<>(new UnrarResponseObject("0"), HttpStatus.NOT_FOUND);
        }
        String queueId = unrarer.addFileToUnrarQueue(dir, new Completion() {
            @Override
            public void success() {
                if (downloadSubs) {
                    downloadSubtitles(dir);
                }
            }

            @Override
            public void fail() {
                //Ignore
            }
        });
        return new ResponseEntity<>(new UnrarResponseObject(queueId), HttpStatus.OK);
    }

    private void downloadSubtitles(File dir) {
        final String absolutePath = unrarer.guessFile(dir, GuessType.PATH);
        final String pathToFile = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
        final String fileName = unrarer.guessFile(dir, GuessType.NAME);
        logger.info("Trying to download subtitles for <{}> to folder: <{}>.", fileName, pathToFile);
        taskExecutor.execute(new SubtitleDownloader(fileName, pathToFile, output -> logger.info(output)));
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


    static {
        logger = LoggerFactory.getLogger(MainController.class);
    }

    public MainController() {

    }
}