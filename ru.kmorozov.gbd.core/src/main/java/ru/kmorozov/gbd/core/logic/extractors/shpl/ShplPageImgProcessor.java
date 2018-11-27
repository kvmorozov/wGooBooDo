package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public class ShplPageImgProcessor extends SimplePageImgProcessor<ShplPage> {

    public ShplPageImgProcessor(final BookContext bookContext, final ShplPage page, final HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }
}
