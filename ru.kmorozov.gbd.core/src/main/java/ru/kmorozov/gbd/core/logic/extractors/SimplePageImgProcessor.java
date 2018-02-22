package ru.kmorozov.gbd.core.logic.extractors;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.io.File;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public class SimplePageImgProcessor<T extends AbstractPage> extends AbstractPageImgProcessor {

    public SimplePageImgProcessor(final BookContext bookContext, final T page, final HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    @Override
    protected String getErrorMsg(final String imgUrl, final HttpHostExt proxy) {
        return String.format("Cannot load data from %s", imgUrl);
    }

    @Override
    protected String getSuccessMsg() {
        return String.format("Finished img processing for %s", page.getPid());
    }

    @Override
    public void run() {
        if (page.dataProcessed.get()) return;

        processImage(page.getImgUrl());
    }

    @Override
    protected boolean validateOutput(final File outputFile, final int width) {
        return true;
    }
}
