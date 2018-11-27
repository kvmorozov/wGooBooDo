package ru.kmorozov.gbd.core.logic.extractors.rfbr;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.db.core.logic.model.book.rfbr.RfbrPage;

import java.util.concurrent.TimeUnit;

public class RfbrImageExtractor extends AbstractImageExtractor {

    public RfbrImageExtractor(final BookContext bookContext) {
        super(bookContext, RfbrImageExtractor.class);
    }

    @Override
    protected void restoreState() {

    }

    @Override
    protected boolean preCheck() {
        return true;
    }

    @Override
    public void newProxyEvent(final HttpHostExt proxy) {
        if (!proxy.isLocal()) return;

        (new Thread(new EventProcessor(proxy))).start();
    }

    private class EventProcessor implements Runnable {

        private final HttpHostExt proxy;

        EventProcessor(HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!RfbrImageExtractor.this.initComplete.get()) {
                RfbrImageExtractor.this.waitingProxy.add(this.proxy);
                return;
            }

            for (IPage page : RfbrImageExtractor.this.bookContext.getBookInfo().getPages().getPages())
                RfbrImageExtractor.this.bookContext.imgExecutor.execute(new RfbrPageImgProcessor(RfbrImageExtractor.this.bookContext, (RfbrPage) page, HttpHostExt.NO_PROXY));

            RfbrImageExtractor.this.bookContext.imgExecutor.terminate(20L, TimeUnit.MINUTES);

            ExecutionContext.INSTANCE.updateProxyList();

            RfbrImageExtractor.this.logger.info(RfbrImageExtractor.this.bookContext.getBookInfo().getPages().getMissingPagesList());

            long pagesAfter = RfbrImageExtractor.this.bookContext.getPagesStream().filter(pageInfo -> pageInfo.isDataProcessed()).count();

            RfbrImageExtractor.this.logger.info(String.format("Processed %s pages", pagesAfter - RfbrImageExtractor.this.bookContext.getPagesBefore()));

            synchronized (RfbrImageExtractor.this.bookContext) {
                ExecutionContext.INSTANCE.postProcessBook(RfbrImageExtractor.this.bookContext);
            }
        }
    }
}
