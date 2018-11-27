package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.db.core.logic.model.book.shpl.ShplPage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplImageExtractor extends AbstractImageExtractor {

    public ShplImageExtractor(BookContext bookContext) {
        super(bookContext, ShplImageExtractor.class);
    }

    @Override
    protected void restoreState() {
        this.bookContext.getPagesStream().forEach(page -> {
            try {
                if (this.bookContext.getStorage().isPageExists(page)) {
                    this.logger.severe(String.format("Page %s found in directory!", page.getPid()));
                    ((AbstractPage) page).setDataProcessed(true);
                    ((AbstractPage) page).setFileExists(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected boolean preCheck() {
        return true;
    }

    @Override
    public void newProxyEvent(HttpHostExt proxy) {
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
            while (!ShplImageExtractor.this.initComplete.get()) {
                ShplImageExtractor.this.waitingProxy.add(this.proxy);
                return;
            }

            for (IPage page : ShplImageExtractor.this.bookContext.getBookInfo().getPages().getPages())
                ShplImageExtractor.this.bookContext.imgExecutor.execute(new ShplPageImgProcessor(ShplImageExtractor.this.bookContext, (ShplPage) page, HttpHostExt.NO_PROXY));

            ShplImageExtractor.this.bookContext.imgExecutor.terminate(20L, TimeUnit.MINUTES);

            ExecutionContext.INSTANCE.updateProxyList();

            ShplImageExtractor.this.logger.info(ShplImageExtractor.this.bookContext.getBookInfo().getPages().getMissingPagesList());

            long pagesAfter = ShplImageExtractor.this.bookContext.getPagesStream().filter(pageInfo -> pageInfo.isDataProcessed()).count();

            ShplImageExtractor.this.logger.info(String.format("Processed %s pages", pagesAfter - ShplImageExtractor.this.bookContext.getPagesBefore()));

            synchronized (ShplImageExtractor.this.bookContext) {
                ExecutionContext.INSTANCE.postProcessBook(ShplImageExtractor.this.bookContext);
            }
        }
    }
}
