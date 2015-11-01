package nu.nldv.unroar;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import nu.nldv.unroar.model.Completion;
import nu.nldv.unroar.model.GuessType;
import nu.nldv.unroar.model.QueueItem;

@Service
@Scope(value = "singleton")
public class Unrarer {

    private final Logger logger;
    private TaskScheduler taskScheduler;
    private TaskExecutor taskExecutor;
    private LinkedList<QueueItem> queue = new LinkedList<>();
    private String unrarPath = MainController.path;
    private File currentWork = null;


    public Unrarer() {
        ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Config.xml");
        taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");
        taskScheduler = (ConcurrentTaskScheduler) context.getBean("taskScheduler");
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public int addFileToUnrarQueue(File dir) {
        assert (dir != null);
        assert (dir.isDirectory());
        QueueItem queueItem = new QueueItem(dir);
        if (!getQueue().contains(queueItem)) {
            logger.info("Adding to queue: " + queueItem);
            getQueue().push(queueItem);
            taskScheduler.schedule(peekInQueue, dateInSeconds(0));
            return dir.hashCode();
        } else {
            logger.info("queue already contains the queueItem: " + queueItem);
            return -1;
        }

    }

    private String unrar(File dir) {
        File rarFile = getRarFile(dir);

        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rarFile));
        } catch (RarException | IOException e) {
            e.printStackTrace();
        }
        if (archive != null) {
            final String resultPath = guessResultPath(archive.getFileHeaders().get(0));
            taskExecutor.execute(new HeavyLifting(archive, new Completion() {
                @Override
                public void success() {
                    super.success();
                    logger.info("Successfully completed extracting " + resultPath);
                    setCurrentWork(null);
                }

                @Override
                public void fail() {
                    super.fail();
                    logger.info("Failed extracting " + resultPath);
                    setCurrentWork(null);
                }
            }));
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

    private String guessResultFileName(FileHeader fh) {
        if (fh != null) {
            File out = new File(unrarPath + File.separator + fh.getFileNameString().trim());
            return out.getName();
        }
        return null;
    }

    public String guessFile(File dir, GuessType type) {
        File rarFile = getRarFile(dir);

        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rarFile));
        } catch (RarException | IOException e) {
            e.printStackTrace();
        }
        if (archive != null) {
            switch (type) {
                case PATH:
                    return guessResultPath(archive.getFileHeaders().get(0));
                case NAME:
                    return guessResultFileName(archive.getFileHeaders().get(0));
                default:
                    return null;
            }
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
                } catch (RarException | IOException e) {
                    e.printStackTrace();
                }
                fh = archive.nextFileHeader();
            }
            completion.success();
        }
    }

    private ScheduledFuture<?> scheduledFuture;
    private Runnable peekInQueue = new Runnable() {
        @Override
        public void run() {
            if (getCurrentWork() != null) {
                logger.info("Already working on something... checking again soon");
                cancelFuture();
                scheduledFuture = taskScheduler.schedule(peekInQueue, dateInSeconds(2));
            } else if (!getQueue().isEmpty()) {
                logger.info("Not working and there is something in queue... starting new work");
                final QueueItem queueItem = getQueue().pop();
                setCurrentWork(queueItem.getDir());
                unrar(queueItem.getDir());
                cancelFuture();
                taskScheduler.schedule(peekInQueue, dateInSeconds(1));
            } else {
                logger.info("Not working and nothing in queue... time for a nap");
            }
        }
    };

    private void cancelFuture() {
        if(scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
    }

    private Date dateInSeconds(int i) {
        return new Date(new Date().getTime() + (i * 1000));
    }

    public synchronized LinkedList<QueueItem> getQueue() {
        return queue;
    }

    public synchronized File getCurrentWork() {
        return currentWork;
    }

    private synchronized void setCurrentWork(File currentWork) {
        this.currentWork = currentWork;
    }
}
