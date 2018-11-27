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

    public SimplePageImgProcessor(BookContext bookContext, T page, HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    @Override
    protected String getErrorMsg(String imgUrl, HttpHostExt proxy) {
        return String.format("Cannot load data from %s", imgUrl);
    }

    @Override
    protected String getSuccessMsg() {
        return String.format("Finished img processing for %s", this.page.getPid());
    }

    @Override
    public void run() {
        if (this.page.isDataProcessed()) return;

        this.processImage(this.page.getImgUrl());
    }

    @Override
    protected boolean validateOutput(IStoredItem storedItem, int width) {
        return true;
    }
}
