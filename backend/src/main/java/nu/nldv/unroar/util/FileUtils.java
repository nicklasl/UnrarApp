package nu.nldv.unroar.util;

import java.io.File;

public class FileUtils {

    public static String constructIdFromFile(File dir) {
        return Md5Hasher.getInstance().hash(dir.getAbsolutePath());
    }

    public static int calculateFileSize(File[] files) {
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

    public static int calculateFileSize(File file) {
        if(file == null) {
            return 0;
        }

        return calculateFileSize(new File[]{file});
    }

}
