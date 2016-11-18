package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.util.concurrent.TimeUnit;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplImageExtractor extends AbstractImageExtractor {

    public ShplImageExtractor(BookContext bookContext) {
        super(bookContext);
        logger = INSTANCE.getLogger(ShplImageExtractor.class, bookContext);
    }

    @Override
    protected boolean preCheck() {
        return true;
    }

    private class EventProcessor implements Runnable {

        private HttpHostExt proxy;

        EventProcessor(HttpHostExt proxy) {
            this.proxy = proxy;
        }

        @Override
        public void run() {
            while (!initComplete.get()) {
                waitingProxy.add(proxy);
                return;
            }

            for (IPage page : bookContext.getBookInfo().getPages().getPages())
                bookContext.imgExecutor.execute(new ShplPageImgProcessor(bookContext, (ShplPage) page, HttpHostExt.NO_PROXY));

            bookContext.imgExecutor.terminate(20, TimeUnit.MINUTES);

            INSTANCE.updateProxyList();

            logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

            long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

            logger.info(String.format("Processed %s pages", pagesAfter - bookContext.getPagesBefore()));

            synchronized (bookContext) {
                INSTANCE.postProcessBook(bookContext);
            }
        }
    }

    @Override
    public void newProxyEvent(HttpHostExt proxy) {
        if (!proxy.isLocal())
            return;

        (new Thread(new EventProcessor(proxy))).start();
    }
}
