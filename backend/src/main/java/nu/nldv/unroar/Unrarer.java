package nu.nldv.unroar;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import nu.nldv.unroar.model.Completion;
import nu.nldv.unroar.model.CurrentWorkUnit;
import nu.nldv.unroar.model.GuessType;
import nu.nldv.unroar.model.QueueItem;
import nu.nldv.unroar.util.Md5Hasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

@Service
@Scope(value = "singleton")
public class Unrarer {

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private TaskExecutor taskExecutor;

    private Logger logger;
    private Queue<QueueItem> queue = new LinkedList<>();
    private CurrentWorkUnit currentWork = null; //TODO need to rewrite to a "currentWorkUnit" with archive, filename etc. to be able to check status.


    public Unrarer() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public String addFileToUnrarQueue(File dir, Completion completion) {
        assert (dir != null);
        assert (dir.isDirectory());
        QueueItem queueItem = new QueueItem(dir, completion);
        if (!getQueue().contains(queueItem)) {
            logger.info("Adding to queue: " + queueItem);
            getQueue().add(queueItem);
            taskScheduler.schedule(peekInQueue, dateInSeconds(0));
            return Md5Hasher.getInstance().hash(dir.getAbsolutePath());
        } else {
            logger.info("queue already contains the queueItem: " + queueItem);
            return "-1";
        }

    }

    private String unrar(QueueItem queueItem) {
        File rarFile = getRarFile(queueItem.getDir());

        Archive archive = null;
        try {
            archive = new Archive(new FileVolumeManager(rarFile));
        } catch (RarException | IOException e) {
            e.printStackTrace();
        }
        if (archive != null && !archive.getFileHeaders().isEmpty()) {
            final String resultPath = guessResultPath(archive.getFileHeaders().get(0), queueItem.getDir());
            setCurrentWork(new CurrentWorkUnit(rarFile, new File(resultPath)));
            final Completion outsideCompletionBlock = queueItem.getCompletion();
            taskExecutor.execute(new HeavyLifting(archive, new Completion() {
                @Override
                public void success() {
                    super.success();
                    logger.info("Successfully completed extracting " + resultPath);
                    setCurrentWork(null);
                    outsideCompletionBlock.success();
                }

                @Override
                public void fail() {
                    super.fail();
                    logger.info("Failed extracting " + resultPath);
                    setCurrentWork(null);

                    outsideCompletionBlock.fail();
                }
            }, queueItem.getDir().getParent()));
            return resultPath;
        } else {
            logger.error("Failed extracting " + rarFile.getAbsolutePath());
        }
        return null;
    }

    private synchronized File getRarFile(File dir) {
        if (dir.isFile() && dir.getAbsolutePath().endsWith(".rar")) {
            return dir;
        }
        final Stream<File> stream = Arrays.stream(dir.listFiles((f, n) -> n.endsWith(".rar")));
        return stream.findFirst().orElseThrow(() -> new RuntimeException("Could not find rar file"));
    }


    private String guessResultPath(FileHeader fh, File dir) {
        if (fh != null) {
            String unrarPath = dir.getParent();
            File out = new File(unrarPath + File.separator + fh.getFileNameString().trim());
            return out.getAbsolutePath();
        }
        return null;
    }

    private String guessResultFileName(FileHeader fh, File dir) {
        if (fh != null) {
            String unrarPath = dir.getParent();
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
        if (archive != null && !archive.getFileHeaders().isEmpty()) {
            switch (type) {
                case PATH:
                    return guessResultPath(archive.getFileHeaders().get(0), dir);
                case NAME:
                    return guessResultFileName(archive.getFileHeaders().get(0), dir);
                default:
                    return null;
            }
        } else return null;

    }

    private class HeavyLifting implements Runnable {
        private final Archive archive;
        private final Completion completion;
        private final String unrarPath;

        public HeavyLifting(Archive archive, Completion completion, String unrarPath) {
            this.archive = archive;
            this.completion = completion;
            this.unrarPath = unrarPath;
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
            final CurrentWorkUnit currentWork = getCurrentWork();
            if (currentWork != null) {
                logger.info("Already working on {}... checking again soon", currentWork);
                cancelFuture();
                scheduledFuture = taskScheduler.schedule(peekInQueue, dateInSeconds(5));
            } else if (!getQueue().isEmpty()) {
                final QueueItem queueItem = getQueue().poll();
                logger.info("Not working and there is something in queue... starting new work: {}", queueItem.getDir().getName());
                unrar(queueItem);
                cancelFuture();
                taskScheduler.schedule(peekInQueue, dateInSeconds(1));
            } else {
                logger.info("Not working and nothing in queue... stop polling");
            }
        }
    };

    private void cancelFuture() {
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
    }

    private Date dateInSeconds(int i) {
        return new Date(new Date().getTime() + (i * 1000));
    }

    public synchronized Queue<QueueItem> getQueue() {
        return queue;
    }

    public synchronized CurrentWorkUnit getCurrentWork() {
        return currentWork;
    }

    private synchronized void setCurrentWork(CurrentWorkUnit currentWork) {
        this.currentWork = currentWork;
    }
}
