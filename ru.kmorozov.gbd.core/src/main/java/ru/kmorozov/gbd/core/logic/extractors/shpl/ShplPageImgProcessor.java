package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.io.File;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public class ShplPageImgProcessor extends AbstractPageImgProcessor<ShplPage> {

    public ShplPageImgProcessor(BookContext bookContext, ShplPage page, HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    @Override
    protected String getErrorMsg(String imgUrl, HttpHostExt proxy) {
        return null;
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
    protected boolean validateOutput(File outputFile, int width) {
        return true;
    }
}
