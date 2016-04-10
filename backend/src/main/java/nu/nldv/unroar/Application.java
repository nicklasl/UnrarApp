package nu.nldv.unroar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(UppackarenConfig.class)
public class Application {

    public static String path;

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Need to supply a directory when starting.");
        }
        path = args[0];
        SpringApplication.run(Application.class, args);
    }

}
