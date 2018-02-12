package ru.kmorozov.gbd.core.logic.extractors.google;

import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.utils.Images;

import java.io.File;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_IMG_TEMPLATE;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageImgProcessor extends AbstractPageImgProcessor<GooglePageInfo> {

    private static final String IMG_ERROR_TEMPLATE = "No img at %s with proxy %s";

    GooglePageImgProcessor(final BookContext bookContext, final GooglePageInfo page, final HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    private boolean processImageWithProxy(final HttpHostExt proxy) {
        return !(!proxy.isLocal() && !proxy.isAvailable()) &&
                processImage(page.getImqRqUrl(bookContext.getBookInfo().getBookId(), HTTPS_IMG_TEMPLATE, getImgWidth()), proxy);
    }

    @Override
    protected String getErrorMsg(final String imgUrl, final HttpHostExt proxy) {
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

    @Override
    protected boolean validateOutput(final File outputFile, final int width) {
        return !Images.isInvalidImage(outputFile, width);
    }
}
