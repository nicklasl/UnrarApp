package nu.nldv.unroar;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import nu.nldv.unroar.model.Completion;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Service
public class Unrarer {

    private TaskExecutor taskExecutor;

    private String unrarPath = MainController.path;

    public Unrarer() {
        ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Config.xml");
        taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");
    }

    public String unrarFileInDir(File dir, Completion completion) {
        assert (dir != null);
        assert (dir.isDirectory());
        File rarFile = getRarFile(dir);

        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rarFile));
        } catch (RarException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (archive != null) {
            String resultPath = guessResultPath(archive.getFileHeaders().get(0));
            taskExecutor.execute(new HeavyLifting(archive, completion));
            return resultPath;
        }
        return null;
    }

    private File getRarFile(File dir) {
        Optional<File> firstOption = Arrays.stream(dir.listFiles((f, n) -> n.endsWith(".rar"))).findFirst();
        assert (firstOption.isPresent());
        return firstOption.get();
    }


    private String guessResultPath(FileHeader fh) {
        if (fh != null) {
            File out = new File(unrarPath + File.separator + fh.getFileNameString().trim());
            return out.getAbsolutePath();
        }
        return null;
    }

    public String guessFileName(File dir) {
        File rarFile = getRarFile(dir);

        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rarFile));
        } catch (RarException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (archive != null) {
            String resultPath = guessResultPath(archive.getFileHeaders().get(0));
            return resultPath;
        } else return null;

    }

    private class HeavyLifting implements Runnable {
        private final Archive archive;
        private final Completion completion;

        public HeavyLifting(Archive archive, Completion completion) {
            this.archive = archive;
            this.completion = completion;
        }

        @Override
        public void run() {
            FileHeader fh = archive.nextFileHeader();
            while (fh != null) {
                try {
                    File out = new File(unrarPath + File.separator + fh.getFileNameString().trim());
                    FileOutputStream os = new FileOutputStream(out);
                    archive.extractFile(fh, os);
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (RarException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fh = archive.nextFileHeader();
            }
            completion.success();
        }
    }
}
