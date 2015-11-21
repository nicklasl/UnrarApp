package nu.nldv.unroar.model;

import java.io.File;
import java.util.List;

import nu.nldv.unroar.util.Md5Hasher;

public class RarArchiveFolder {

    private String id;
    private String name;
    private int numberOfFiles;
    private int dirSizeInMB;
    private List<RarArchiveFolder> subFolders;

    public RarArchiveFolder(File dir, List<RarArchiveFolder> subDirs) {
        this.id = constructIdFromFile(dir);
        this.name = dir.getName();
        File[] files = dir.listFiles();
        this.dirSizeInMB = calculateDirSize(files);
        if (files != null) {
            this.numberOfFiles = files.length;
        } else {
            this.numberOfFiles = 0;
        }
        this.subFolders = subDirs;
    }

    public static String constructIdFromFile(File dir) {
        return Md5Hasher.getInstance().hash(dir.getAbsolutePath());
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

    public List<RarArchiveFolder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<RarArchiveFolder> subFolders) {
        this.subFolders = subFolders;
    }
}
