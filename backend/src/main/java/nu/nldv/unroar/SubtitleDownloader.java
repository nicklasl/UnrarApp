package nu.nldv.unroar;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Doesn't actually do anything other than rely on subliminal (sudo pip install -U subliminal)
 */
public class SubtitleDownloader implements Runnable {

    private static final String CMD = "subliminal download -s -p opensubtitles -l en -d ";

    private final String fileName;
    private final SubtitleDownloaderHandler handler;
    private final String path;

    public SubtitleDownloader(String fileName, String path, SubtitleDownloaderHandler handler) {
        this.fileName = fileName;
        this.path = path;
        this.handler = handler;
    }

    @Override
    public void run() {
        //TODO sanitize fileName and path to protect against shell code injection
        String output = executeShellCommand(CMD + path + " " + fileName);
        if (this.handler != null) {
            handler.handleOutput(output);
        }
    }

    private String executeShellCommand(String command) {
        StringBuilder output = new StringBuilder();

        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = null;
            if (process.exitValue() == 0) {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public interface SubtitleDownloaderHandler {

        void handleOutput(String output);
    }
}
