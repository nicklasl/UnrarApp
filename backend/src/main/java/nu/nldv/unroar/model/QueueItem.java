package nu.nldv.unroar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

public class QueueItem {
    private int id;
    private File dir;

    @JsonIgnore
    private Completion completion;

    public QueueItem(File dir, Completion completion) {
        this.id = dir.hashCode();
        this.dir = dir;
        this.completion = completion;
    }

    public int getId() {
        return id;
    }

    public File getDir() {
        return dir;
    }

    @JsonIgnore
    public Completion getCompletion() {
        return completion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setCompletion(Completion completion) {
        this.completion = completion;
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
        return id;
    }

    @Override
    public String toString() {
        return "QueueItem: " + dir.getAbsolutePath();
    }
}
