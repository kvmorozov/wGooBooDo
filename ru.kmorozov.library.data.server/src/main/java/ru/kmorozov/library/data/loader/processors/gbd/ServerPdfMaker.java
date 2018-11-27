package ru.kmorozov.library.data.loader.processors.gbd;

import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;

public class ServerPdfMaker implements IPostProcessor {

    @Override
    public void make() {

    }

    @Override
    public IPostProcessor getPostProcessor(final BookContext bookContext) {
        return null;
    }

    @Override
    public BookContext getUniqueObject() {
        return null;
    }

    @Override
    public void run() {

    }
}
