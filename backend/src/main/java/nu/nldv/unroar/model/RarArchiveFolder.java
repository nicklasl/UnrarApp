package nu.nldv.unroar.model;

import java.io.File;

import nu.nldv.unroar.filter.NoHiddenFilesFilter;
import nu.nldv.unroar.util.FileUtils;

public class RarArchiveFolder {

    private static final NoHiddenFilesFilter NO_HIDDEN_FILES_FILTER = new NoHiddenFilesFilter();

    private final boolean hasSubDirs;
    private String id;
    private String name;
    private int numberOfFiles;
    private int dirSizeInMB;

    public RarArchiveFolder(File dir, boolean subDir) {
        this.id = FileUtils.constructIdFromFile(dir);
        this.name = dir.getName();
        File[] files = dir.listFiles(NO_HIDDEN_FILES_FILTER);
        this.dirSizeInMB = FileUtils.calculateFileSize(files);
        if (files != null) {
            this.numberOfFiles = files.length;
        } else {
            this.numberOfFiles = 0;
        }
        this.hasSubDirs = subDir;
    }

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
}
