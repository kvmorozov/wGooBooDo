package ru.kmorozov.gbd.core.logic.extractors.google;

import com.google.gson.JsonParseException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hc.core5.http.NoHttpResponseException;
import ru.kmorozov.db.core.logic.model.book.google.GooglePageInfo;
import ru.kmorozov.db.core.logic.model.book.google.GooglePagesInfo;
import ru.kmorozov.db.utils.Mapper;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractHttpProcessor;
import ru.kmorozov.gbd.core.logic.extractors.base.IUniqueRunnable;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.logger.progress.IProgress;
import ru.kmorozov.gbd.utils.QueuedThreadPoolExecutor;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.BOOK_ID_PLACEHOLDER;
import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.HTTPS_TEMPLATE;
import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.PAGES_REQUEST_TEMPLATE;
import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.RQ_PG_PLACEHOLDER;

/**
 * Created by km on 21.11.2015.
 */
class GooglePageSigProcessor extends AbstractHttpProcessor implements IUniqueRunnable<GooglePageSigProcessor> {

    protected static final Logger logger = ExecutionContext.INSTANCE.getLogger(GooglePageSigProcessor.class);

    private static final String SIG_ERROR_TEMPLATE = "No sig at %s with proxy %s";
    private static final String SIG_WRONG_FORMAT = "Wrong sig format: %s";

    private final HttpHostExt proxy;
    private final BookContext bookContext;
    private final QueuedThreadPoolExecutor<GooglePageInfo> sigPageExecutor;

    GooglePageSigProcessor(final BookContext bookContext, final HttpHostExt proxy) {
        this.bookContext = bookContext;
        this.proxy = proxy;

        sigPageExecutor = new QueuedThreadPoolExecutor<>(bookContext.getPagesStream().filter(p -> ((AbstractPage) p).isNotProcessed()).count(), QueuedThreadPoolExecutor.THREAD_POOL_SIZE, GooglePageInfo::isProcessed,
                bookContext.toString() + '/' + proxy);
    }

    @Override
    public void run() {
        if ((GBDOptions.secureMode() && proxy.isLocal()) || !proxy.isAvailable()) return;

        if (!proxy.isLocal() && !(proxy.isAvailable() && 0 < proxy.getHost().getPort())) return;

        final IProgress psSigs = bookContext.getProgress().getSubProgress(bookContext.getBookInfo().getPages().getPages().length);

        bookContext.getPagesStream().filter(p -> ((AbstractPage) p).isNotProcessed()).forEach(page -> {
            psSigs.inc();
            sigPageExecutor.execute(new SigProcessorInternal((GooglePageInfo) page));
        });
        sigPageExecutor.terminate(3L, TimeUnit.MINUTES);

        psSigs.finish();
    }

    @Override
    public GooglePageSigProcessor getUniqueObject() {
        return this;
    }

    @Override
    public String toString() {
        return "Sig processor:" + bookContext;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (null == o || getClass() != o.getClass()) return false;

        final GooglePageSigProcessor that = (GooglePageSigProcessor) o;

        return new EqualsBuilder().append(proxy, that.proxy).append(bookContext, that.bookContext).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(proxy).append(bookContext).toHashCode();
    }

    private class SigProcessorInternal implements IUniqueRunnable<GooglePageInfo> {

        private final GooglePageInfo page;

        SigProcessorInternal(final GooglePageInfo page) {
            this.page = page;
        }

        @Override
        public void run() {
            if (!proxy.isAvailable()) return;

            if (page.isDataProcessed() || null != page.getSig() || page.isSigChecked() || page.isLoadingStarted())
                return;

            Response resp = null;
            final String baseUrl = HTTPS_TEMPLATE.replace(BOOK_ID_PLACEHOLDER, bookContext.getBookInfo().getBookId());
            final String rqUrl = baseUrl + PAGES_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, page.getPid());

            try {
                resp = AbstractHttpProcessor.getContent(rqUrl, proxy, true);
                if (null == resp || null == resp.getContent()) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()));
                    return;
                }

                String respStr = null;
                try (InputStream is = resp.getContent()) {
                    respStr = new String(is.readAllBytes(), Charset.defaultCharset());
                } catch (SocketException | SSLException se) {

                }

                if (respStr.isBlank()) {
                    logger.finest(String.format(SIG_ERROR_TEMPLATE, rqUrl, proxy.toString()));
                    return;
                }

                GooglePagesInfo framePages = null;
                try {
                    framePages = Mapper.getGson().fromJson(respStr, GooglePagesInfo.class);
                } catch (final JsonParseException jpe) {
                    logger.severe("Invalid JSON string: " + respStr);
                }

                if (null == framePages) return;

                Arrays.asList(framePages.getPages())
                        .stream()
                        .filter(page -> null != page.getSrc())
                        .forEach(framePage -> {
                            final GooglePageInfo _page = (GooglePageInfo) bookContext.getBookInfo().getPages().getPageByPid(framePage.getPid());

                            if (_page.isDataProcessed()) return;

                            final String _frameSrc = framePage.getSrc();
                            if (null != _frameSrc) _page.setSrc(_frameSrc);

                            if (null != _page.getSig()) {
                                if (_page.getPid().equals(page.getPid())) {
                                    _page.setSigChecked(true);

                                    proxy.promoteProxy();

                                    // Если есть возможность - пытаемся грузить страницу сразу
                                    bookContext.imgExecutor.execute(new GooglePageImgProcessor(bookContext, _page, proxy));
                                }
                            }

                            if (null != _page.getSrc() && null == _page.getSig())
                                logger.finest(String.format(SIG_WRONG_FORMAT, _page.getSrc()));
                        });
            } catch (SocketTimeoutException | SocketException | NoHttpResponseException ce) {
                if (!proxy.isLocal()) {
                    proxy.registerFailure();
                    logger.info(String.format("Proxy %s failed!", proxy.toString()));
                }

                if (!(ce instanceof SocketTimeoutException)) ce.printStackTrace();
            } catch (final Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (null != resp) resp.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public GooglePageInfo getUniqueObject() {
            return page;
        }
    }
}
