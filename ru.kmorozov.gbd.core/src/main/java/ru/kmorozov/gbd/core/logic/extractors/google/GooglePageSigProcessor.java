package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.gson.JsonParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hc.core5.http.NoHttpResponseException;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.gbd.core.logic.progress.IProgress;
import ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor;
import ru.kmorozov.gbd.core.utils.gson.Mapper;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor.HTTPS_TEMPLATE;
import static ru.kmorozov.gbd.core.utils.QueuedThreadPoolExecutor.THREAD_POOL_SIZE;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageSigProcessor extends AbstractHttpProcessor implements IUniqueRunnable<GooglePageSigProcessor> {

    private static final String SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s";
    private static final String SIG_WRONG_FORMAT = "Wrong sig format: %s";

    private final HttpHostExt proxy;
    private final BookContext bookContext;
    private final QueuedThreadPoolExecutor<GooglePageInfo> sigPageExecutor;

    public GooglePageSigProcessor(BookContext bookContext, HttpHostExt proxy) {
        this.bookContext = bookContext;
        this.proxy = proxy;

        sigPageExecutor = new QueuedThreadPoolExecutor<>(bookContext.getPagesStream().count(), THREAD_POOL_SIZE, GooglePageInfo::isSigChecked, bookContext.toString() + "/" + proxy.toString());
    }

    private class SigProcessorInternal implements IUniqueRunnable<GooglePageInfo> {

        private GooglePageInfo page;

        SigProcessorInternal(GooglePageInfo page) {
            this.page = page;
        }

        @Override
        public void run() {
            if (!proxy.isAvailable()) return;

            if (page.dataProcessed.get() || page.getSig() != null || page.sigChecked.get() || page.loadingStarted.get())
                return;

            Response resp = null;
            String baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookContext.getBookInfo().getBookId());
            String rqUrl = baseUrl + GoogleImageExtractor.PAGES_REQUEST_TEMPLATE.replace(GoogleImageExtractor.RQ_PG_PLACEHOLDER, page.getPid());

            try {
                resp = getContent(rqUrl, proxy, true);
                if (resp == null || resp.getContent() == null) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()));
                    return;
                }

                String respStr = null;
                try (InputStream is = resp.getContent()) {
                    respStr = IOUtils.toString(is, Charset.defaultCharset());
                } catch (SocketException | SSLException se) {

                }

                if (StringUtils.isEmpty(respStr)) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()));
                    return;
                }

                GooglePagesInfo framePages = Mapper.getGson().fromJson(respStr, GooglePagesInfo.class);

                GooglePageInfo[] pages = framePages.getPages();
                for (GooglePageInfo framePage : pages)
                    if (framePage.getSrc() != null) {
                        GooglePageInfo _page = (GooglePageInfo) bookContext.getBookInfo().getPages().getPageByPid(framePage.getPid());

                        if (_page.dataProcessed.get()) continue;

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
            } catch (JsonParseException | SocketTimeoutException | SocketException | NoHttpResponseException ce) {
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
        public GooglePageInfo getUniqueObject() {
            return page;
        }
    }

    @Override
    public void run() {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return;

        if (!proxy.isLocal() && !(proxy.isAvailable() && proxy.getHost().getPort() > 0)) return;

        final IProgress psSigs = bookContext.getProgress().getSubProgress(bookContext.getBookInfo().getPages().getPages().length);

        bookContext.getPagesStream().forEach(page -> {
            psSigs.inc();
            sigPageExecutor.execute(new SigProcessorInternal((GooglePageInfo) page));
        });
        sigPageExecutor.terminate(3, TimeUnit.MINUTES);

        psSigs.finish();
    }

    @Override
    public GooglePageSigProcessor getUniqueObject() {
        return this;
    }

    @Override
    public String toString() {
        return "Sig processor:" + bookContext.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GooglePageSigProcessor that = (GooglePageSigProcessor) o;

        return new EqualsBuilder().append(proxy, that.proxy).append(bookContext, that.bookContext).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(proxy).append(bookContext).toHashCode();
    }
}
