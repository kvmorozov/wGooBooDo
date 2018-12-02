package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.util.concurrent.TimeUnit;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplImageExtractor extends AbstractImageExtractor {

    public ShplImageExtractor(final BookContext bookContext) {
        super(bookContext, ShplImageExtractor.class);
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

        EventProcessor(final HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy);
                return;
            }

            for (final IPage page : bookContext.getBookInfo().getPages().getPages())
                bookContext.imgExecutor.execute(new ShplPageImgProcessor(bookContext, (ShplPage) page, HttpHostExt.NO_PROXY));

            bookContext.imgExecutor.terminate(20L, TimeUnit.MINUTES);

            ExecutionContext.INSTANCE.updateProxyList();

            logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

            final long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.isDataProcessed()).count();

            logger.info(String.format("Processed %s pages", pagesAfter - bookContext.getPagesBefore()));

            synchronized (bookContext) {
                ExecutionContext.INSTANCE.postProcessBook(bookContext);
            }
        }
    }
}
