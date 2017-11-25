package ru.kmorozov.gbd.core.logic.extractors.shpl;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.shpl.ShplPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplImageExtractor extends AbstractImageExtractor {

    public static final int DEFAULT_PAGE_WIDTH = 7;

    public ShplImageExtractor(BookContext bookContext) {
        super(bookContext);
        logger = INSTANCE.getLogger(ShplImageExtractor.class, bookContext);
    }

    @Override
    protected void scanDir() {
        Path outputPath = Paths.get(bookContext.getOutputDir().toURI());

        bookContext.getPagesStream().forEach(page -> {
            try {
                if (Files.find(outputPath, 1, (path, basicFileAttributes) -> path.toString().contains("\\" + page.getOrder() + "_" + page.getPid() + "."), FOLLOW_LINKS).count() == 1) {
                    logger.severe(String.format("Page %s found in directory!", page.getPid()));
                    page.dataProcessed.set(true);
                    page.fileExists.set(true);
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
}
