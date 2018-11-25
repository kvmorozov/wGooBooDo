package ru.kmorozov.library.data.loader.processors.gbd;

import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.loader.DirContextLoader;
import ru.kmorozov.library.data.loader.processors.IGbdProcessor;

@Component
public class GbdLocalProcessor implements IGbdProcessor {

    private static transient DirContextLoader googleBooksLoader;

    @Override
    public void load(String bookId) {

    }

    @Override
    public void process() {

    }
}
