package nu.nldv.unroar.filter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;

@Service
@Scope(value = "singleton")
public class RarFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".rar");
    }
}
