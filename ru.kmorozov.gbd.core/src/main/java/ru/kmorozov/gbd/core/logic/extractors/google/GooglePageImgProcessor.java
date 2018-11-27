package ru.kmorozov.gbd.core.logic.extractors.google;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractPageImgProcessor;
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.utils.Images;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_IMG_TEMPLATE;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageImgProcessor extends AbstractPageImgProcessor<GooglePageInfo> {

    private static final String IMG_ERROR_TEMPLATE = "No img at %s with proxy %s";

    GooglePageImgProcessor(BookContext bookContext, GooglePageInfo page, HttpHostExt usedProxy) {
        super(bookContext, page, usedProxy);
    }

    private boolean processImageWithProxy(HttpHostExt proxy) {
        return !(!proxy.isLocal() && !proxy.isAvailable()) &&
                this.processImage(this.page.getImqRqUrl(this.bookContext.getBookInfo().getBookId(), HTTPS_IMG_TEMPLATE, AbstractPageImgProcessor.getImgWidth()), proxy);
    }

    @Override
    protected String getErrorMsg(String imgUrl, HttpHostExt proxy) {
        return String.format(GooglePageImgProcessor.IMG_ERROR_TEMPLATE, imgUrl, proxy.toString());
    }

    @Override
    protected String getSuccessMsg() {
        return String.format("Finished img processing for %s%s", this.page.getPid(), this.page.isGapPage() ? " with gap" : "");
    }

    @Override
    public void run() {
        if (this.page.isDataProcessed()) return;

        if (!this.processImageWithProxy(this.usedProxy)) {
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (!this.usedProxy.isLocal()) if (!this.processImageWithProxy(HttpHostExt.NO_PROXY))
                // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
                AbstractProxyListProvider.getInstance().getParallelProxyStream().forEach(proxy -> {
                    if (proxy != this.usedProxy) if (this.processImageWithProxy(proxy)) {
                    }
                });
        }
    }

    @Override
    protected boolean validateOutput(IStoredItem storedItem, int width) {
        return !Images.isInvalidImage(storedItem.asFile(), width);
    }
}
