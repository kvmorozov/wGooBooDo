package ru.kmorozov.gbd.core.logic.extractors.google;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.NoHttpResponseException;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.IUniqueRunnable;
import ru.kmorozov.gbd.core.logic.model.book.PageInfo;
import ru.kmorozov.gbd.core.logic.model.book.PagesInfo;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.Mapper;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageSigProcessor extends AbstractHttpProcessor implements IUniqueRunnable<BookContext> {

    private static final String SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s";
    private static final String SIG_WRONG_FORMAT = "Wrong sig format: %s";

    private final HttpHostExt proxy;
    private final BookContext bookContext;

    public GooglePageSigProcessor(BookContext bookContext, HttpHostExt proxy) {
        this.bookContext = bookContext;
        this.proxy = proxy;
    }

    private void getSig(PageInfo page) {
        if (!proxy.isAvailable()) return;

        if (page.dataProcessed.get() || page.getSig() != null || page.sigChecked.get() || page.loadingStarted.get())
            return;

        Response resp = null;
        String rqUrl = bookContext.getBaseUrl() + GoogleImageExtractor.PAGES_REQUEST_TEMPLATE.replace(GoogleImageExtractor.RQ_PG_PLACEHOLDER, page.getPid());

        try {
            resp = getContent(rqUrl, proxy, true);
            if (resp == null) {
                logger.info(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()));
                return;
            }

            String respStr = IOUtils.toString(resp.getContent(), Charset.defaultCharset());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            PageInfo[] pages = framePages.getPages();
            for (PageInfo framePage : pages)
                if (framePage.getOrder() >= page.getOrder() && framePage.getSrc() != null) {
                    PageInfo _page = bookContext.getBookInfo().getPages().getPageByPid(framePage.getPid());

                    if (_page.dataProcessed.get() || _page.getSig() != null || _page.sigChecked.get()) continue;

                    String _frameSrc = framePage.getSrc();
                    if (_frameSrc != null) _page.setSrc(_frameSrc);

                    if (_page.getSig() != null) {
                        if (_page.getPid().equals(page.getPid())) {
                            _page.sigChecked.set(true);

                            proxy.promoteProxy();

                            // Если есть возможность - пытаемся грузить страницу сразу
                            bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, _page, proxy));
                        }
                    }

                    if (_page.getSrc() != null && _page.getSig() == null)
                        logger.finest(String.format(SIG_WRONG_FORMAT, _page.getSrc()));
                }
        } catch (JsonParseException | JsonMappingException | SocketTimeoutException | SocketException | NoHttpResponseException ce) {
            if (!proxy.isLocal()) {
                proxy.registerFailure();
                logger.info(String.format("Proxy %s failed!", proxy.toString()));
            }

            if (!(ce instanceof SocketTimeoutException)) ce.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (resp != null) resp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return;

        if (!proxy.isLocal() && !(proxy.isAvailable() && proxy.getHost().getPort() > 0)) return;

        final IProgress psSigs = bookContext.getProgress().getSubProgress(bookContext.getBookInfo().getPages().getPages().length);

        ExecutorService sigPool = Executors.newCachedThreadPool();
        sigPool.submit(() -> bookContext.getPagesParallelStream().forEach(page -> {
            psSigs.inc();
            getSig(page);
        }));

        sigPool.shutdown();
        try {
            sigPool.awaitTermination(100, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }

        psSigs.finish();
    }

    @Override
    public BookContext getUniqueObject() {
        return bookContext;
    }

    @Override
    public String toString() {
        return "Sig processor:" + bookContext.toString();
    }
}
