package nu.nldv.uppackaren.model;

import java.io.Serializable;

public class RarArchive implements Serializable{

    private String id;
    private String name;
    private int numberOfFiles;
    private int dirSizeInMB;
    private boolean hasSubDirs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public int getDirSizeInMB() {
        return dirSizeInMB;
    }

    public void setDirSizeInMB(int dirSizeInMB) {
        this.dirSizeInMB = dirSizeInMB;
    }

    public boolean isHasSubDirs() {
        return hasSubDirs;
    }

    public void setHasSubDirs(boolean hasSubDirs) {
        this.hasSubDirs = hasSubDirs;
    }
}
