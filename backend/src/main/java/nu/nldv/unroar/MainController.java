package nu.nldv.unroar;

import nu.nldv.unroar.model.Completion;
import nu.nldv.unroar.model.QueueItem;
import nu.nldv.unroar.model.RarArchiveFolder;
import nu.nldv.unroar.model.UnrarResponseObject;
import nu.nldv.unroar.model.UnrarStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

@Controller
@ComponentScan
@EnableAutoConfiguration
public class MainController {

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
        List<RarArchiveFolder> archiveFolders = new ArrayList<>();
        File currentDir = new File(path);
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File dir : files) {
                if (dir.isDirectory()
                        && containsRarFiles(dir)
                        && !alreadyUnpacked(dir, files)) {
                    archiveFolders.add(new RarArchiveFolder(dir));
                }
            }
        }


        return archiveFolders;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<UnrarResponseObject> unRarArchive(@PathVariable final String id) {
        File dir = findFileById(id);
        if (dir == null) {
            return new ResponseEntity<>(new UnrarResponseObject(0), HttpStatus.NOT_FOUND);
        }

        int queueId = unrarer.addFileToUnrarQueue(dir, new Completion() {
            @Override
            public void success() {
                super.success();
            }

            @Override
            public void fail() {
                super.fail();
            }
        });
        return new ResponseEntity<>(new UnrarResponseObject(queueId), HttpStatus.OK);
    }

    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    public ResponseEntity<List<QueueItem>> getQueue() {
        return new ResponseEntity<List<QueueItem>>(unrarer.getQueue(), HttpStatus.OK);
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity getUnpackingStatusForId() throws IOException {
        final File currentWorkFile = unrarer.getCurrentWork();
        if (currentWorkFile == null) {
            return ResponseEntity.notFound().build();
        }
        final String fileName = unrarer.guessFileName(currentWorkFile);
        final File newFile = new File(fileName);
        if (newFile.exists()) {
            final int currentSizeOfFile = RarArchiveFolder.calculateDirSize(new File[]{newFile});
            final float percentDone = (float) currentSizeOfFile / (float) RarArchiveFolder.calculateDirSize(currentWorkFile.listFiles());
            return new ResponseEntity<>(new UnrarStatus((int) (percentDone * 100)), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean alreadyUnpacked(File dir, File[] files) {
        String unpackedFileName = unrarer.guessFileName(dir);
        boolean result = containsFileWithName(files, unpackedFileName);
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

    private File findFileById(String id) {
        File currentDir = new File(path);
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File dir : files) {
                if (RarArchiveFolder.constructIdFromFile(dir).equalsIgnoreCase(id)) {
                    return dir;
                }
            }
        }
        return null;
    }


    private boolean containsRarFiles(File dir) {
        if (dir == null) {
            return false;
        } else {
            final FilenameFilter filenameFilter = (f, n) -> n.endsWith(".rar");
            boolean b = Arrays.stream(dir.listFiles(filenameFilter)).count() > 0;
            return b;
        }
    }

    public static void main(String[] args) throws Exception {
        if(args == null || args.length == 0) {
            throw new IllegalArgumentException("Need to supply a directory when starting.");
        }
        path = args[0];
        SpringApplication.run(MainController.class, args);
    }
}