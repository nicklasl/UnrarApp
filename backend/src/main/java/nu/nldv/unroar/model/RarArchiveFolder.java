package nu.nldv.unroar.model;

import java.io.File;

import nu.nldv.unroar.filter.NoHiddenFilesFilter;
import nu.nldv.unroar.util.Md5Hasher;

public class RarArchiveFolder {

    private static final NoHiddenFilesFilter NO_HIDDEN_FILES_FILTER = new NoHiddenFilesFilter();

    private final boolean hasSubDirs;
    private String id;
    private String name;
    private int numberOfFiles;
    private int dirSizeInMB;

    public RarArchiveFolder(File dir, boolean subDir) {
        this.id = constructIdFromFile(dir);
        this.name = dir.getName();
        File[] files = dir.listFiles(NO_HIDDEN_FILES_FILTER);
        this.dirSizeInMB = calculateDirSize(files);
        if (files != null) {
            this.numberOfFiles = files.length;
        } else {
            this.numberOfFiles = 0;
        }
        this.hasSubDirs = subDir;
    }

    public static String constructIdFromFile(File dir) {
        return Md5Hasher.getInstance().hash(dir.getAbsolutePath());
    }

    public static int calculateDirSize(File[] files) {
        if(files == null) {
            return 0;
        }
        double total = 0;
        for (File file : files) {
            double bytes = file.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            total += megabytes;
        }

        return (int) total;
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
