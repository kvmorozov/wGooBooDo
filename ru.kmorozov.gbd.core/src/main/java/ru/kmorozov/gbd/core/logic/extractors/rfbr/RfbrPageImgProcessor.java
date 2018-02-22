package ru.kmorozov.gbd.core.logic.extractors.rfbr;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.SimplePageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.rfbr.RfbrPage;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public class RfbrPageImgProcessor extends SimplePageImgProcessor<RfbrPage> {

    public RfbrPageImgProcessor(BookContext bookContext, RfbrPage page, HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }
}
