package ru.kmorozov.gbd.core.logic.extractors;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

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
        if (page.isDataProcessed()) return;

        processImage(page.getImgUrl());
    }

    @Override
    protected boolean validateOutput(final IStoredItem storedItem, final int width) {
        return true;
    }
}
