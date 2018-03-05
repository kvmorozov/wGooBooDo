package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplImageExtractor extends AbstractImageExtractor {

    public ShplImageExtractor(final BookContext bookContext) {
        super(bookContext, ShplImageExtractor.class);
    }

    @Override
    protected void scanDir() {
        bookContext.getPagesStream().forEach(page -> {
            try {
                if (bookContext.getStorage().isPageExists(page)) {
                    logger.severe(String.format("Page %s found in directory!", page.getPid()));
                    page.dataProcessed.set(true);
                    page.fileExists.set(true);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });
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

            bookContext.imgExecutor.terminate(20, TimeUnit.MINUTES);

            ExecutionContext.INSTANCE.updateProxyList();

            logger.info(bookContext.getBookInfo().getPages().getMissingPagesList());

            final long pagesAfter = bookContext.getPagesStream().filter(pageInfo -> pageInfo.dataProcessed.get()).count();

            logger.info(String.format("Processed %s pages", pagesAfter - bookContext.getPagesBefore()));

            synchronized (bookContext) {
                ExecutionContext.INSTANCE.postProcessBook(bookContext);
            }
        }
    }
}
