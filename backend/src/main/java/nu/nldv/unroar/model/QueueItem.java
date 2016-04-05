package nu.nldv.unroar.model;

import java.io.File;

import nu.nldv.unroar.util.Md5Hasher;

public class QueueItem {
    private final Completion completion;
    private final String id;
    private final File dir;

    public QueueItem(File dir, Completion completion) {
        this.id = Md5Hasher.getInstance().hash(dir.getAbsolutePath());
        this.dir = dir;
        this.completion = completion;
    }

    public String getId() {
        return id;
    }

    public File getDir() {
        return dir;
    }

    public Completion getCompletion() {
        return completion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueueItem queueItem = (QueueItem) o;

        return id == queueItem.id;

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "QueueItem: " + dir.getAbsolutePath();
    }
}
