package ru.kmorozov.gbd.core.logic.extractors.google;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.google.GogglePageInfo;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageImgProcessor extends AbstractPageImgProcessor<GogglePageInfo> {

    private static final String IMG_ERROR_TEMPLATE = "No img at %s with proxy %s";

    public GooglePageImgProcessor(BookContext bookContext, GogglePageInfo page, HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    private boolean processImageWithProxy(HttpHostExt proxy) {
        return !(!proxy.isLocal() && !proxy.isAvailable()) && processImage(page.getImqRqUrl(bookContext.getBookInfo().getBookId(), GoogleImageExtractor.HTTPS_TEMPLATE, GBDOptions.getImageWidth()), proxy);
    }

    @Override
    protected String getErrorMsg(String imgUrl, HttpHostExt proxy) {
        return String.format(IMG_ERROR_TEMPLATE, imgUrl, proxy.toString());
    }

    @Override
    protected String getSuccessMsg() {
        return String.format("Finished img processing for %s%s", page.getPid(), page.isGapPage() ? " with gap" : "");
    }

    @Override
    public void run() {
        if (page.dataProcessed.get()) return;

        if (!processImageWithProxy(usedProxy)) {
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (!usedProxy.isLocal()) if (!processImageWithProxy(HttpHostExt.NO_PROXY))
                // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
                AbstractProxyListProvider.getInstance().getParallelProxyStream().forEach(proxy -> {
                    if (proxy != usedProxy) if (processImageWithProxy(proxy)) {
                    }
                });
        }
    }
}