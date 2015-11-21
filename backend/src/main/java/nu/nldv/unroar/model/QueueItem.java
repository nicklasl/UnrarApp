package nu.nldv.unroar.model;

import java.io.File;

import nu.nldv.unroar.util.Md5Hasher;

public class QueueItem {
    private String id;
    private File dir;

    public QueueItem(File dir) {
        this.id = Md5Hasher.getInstance().hash(dir.getAbsolutePath());
        this.dir = dir;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
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
