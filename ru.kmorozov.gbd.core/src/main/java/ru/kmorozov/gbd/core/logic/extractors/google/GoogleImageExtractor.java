package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.common.base.Strings;
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 21.11.2015.
 */
public class GoogleImageExtractor extends AbstractImageExtractor {

    private final AtomicInteger proxyReceived = new AtomicInteger(0);
    private final AtomicBoolean processingStarted = new AtomicBoolean(false);

    public GoogleImageExtractor(final BookContext bookContext) {
        super(bookContext, GoogleImageExtractor.class);
    }

    @Override
    protected boolean preCheck() {
        if (!Strings.isNullOrEmpty(((GoogleBookData) bookContext.getBookInfo().getBookData()).getFlags().getDownloadPdfUrl())) {
            logger.severe("There is direct url to download book. DIY!");
            return false;
        } else return true;
    }

    @Override
    protected void prepareStorage() {
        super.prepareStorage();
        bookContext.getBookInfo().getPages().build();
    }

    @Override
    public void newProxyEvent(final HttpHostExt proxy) {
        if (null != proxy)
            (new Thread(new EventProcessor(proxy))).start();
    }

    private class EventProcessor implements Runnable {

        private final HttpHostExt proxy;

        EventProcessor(final HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy);
                return;
            }

            if (proxy.isAvailable()) bookContext.sigExecutor.execute(new GooglePageSigProcessor(bookContext, proxy));

            final int proxyNeeded = ExecutionContext.getProxyCount() - proxyReceived.incrementAndGet();

            if (0 >= proxyNeeded) {
                if (!processingStarted.compareAndSet(false, true)) return;

                bookContext.sigExecutor.terminate(10L, TimeUnit.MINUTES);

                bookContext.getPagesStream().filter(page -> !page.isDataProcessed() && null != ((GooglePageInfo) page).getSig()).forEach(page -> bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, (GooglePageInfo) page, HttpHostExt.NO_PROXY)));

                bookContext.imgExecutor.terminate(10L, TimeUnit.MINUTES);

                logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

                final long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.isDataProcessed()).count();

                bookContext.setPagesProcessed(pagesAfter - bookContext.getPagesBefore());
                logger.info(String.format("Processed %s pages", bookContext.getPagesProcessed()));

                ExecutionContext.INSTANCE.postProcessBook(bookContext);
            }
        }
    }
}
