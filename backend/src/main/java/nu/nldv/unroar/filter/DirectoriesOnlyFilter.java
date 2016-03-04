package nu.nldv.unroar.filter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;

@Service
@Scope(value = "singleton")
public class DirectoriesOnlyFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}
