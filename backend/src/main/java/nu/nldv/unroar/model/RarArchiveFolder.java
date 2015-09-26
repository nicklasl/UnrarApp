package nu.nldv.unroar.model;

import java.io.File;

public class RarArchiveFolder {

    private String id;
    private String name;
    private int numberOfFiles;
    private int dirSizeInMB;

    public RarArchiveFolder(File dir) {
        this.id = constructIdFromFile(dir);
        this.name = dir.getName();
        File[] files = dir.listFiles();
        this.dirSizeInMB = calculateDirSize(files);
        if (files != null) {
            this.numberOfFiles = files.length;
        } else {
            this.numberOfFiles = 0;
        }
    }

    public static String constructIdFromFile(File dir) {
        return Integer.toString(dir.getAbsolutePath().hashCode());
    }

    public static int calculateDirSize(File[] files) {
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
}
