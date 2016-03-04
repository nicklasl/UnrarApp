package nu.nldv.unroar.model;

import java.io.File;

public class CurrentWorkUnit {
    private File archiveFile;
    private File unpackedFile;

    public CurrentWorkUnit(File rarFile, File guessedResultFile) {
        this.archiveFile = rarFile;
        this.unpackedFile = guessedResultFile;
    }

    public File getArchiveFile() {
        return archiveFile;
    }

    public void setArchiveFile(File archiveFile) {
        this.archiveFile = archiveFile;
    }

    public File getUnpackedFile() {
        return unpackedFile;
    }

    public void setUnpackedFile(File unpackedFile) {
        this.unpackedFile = unpackedFile;
    }

    @Override
    public String toString() {
        return "CurrentWorkUnit{" +
                "archiveFile=" + archiveFile +
                ", unpackedFile=" + unpackedFile +
                '}';
    }
}
