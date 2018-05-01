package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.Images;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public abstract class AbstractPageImgProcessor<T extends AbstractPage> extends AbstractHttpProcessor implements IUniqueRunnable<T> {

    private static final int dataChunk = 4096;

    protected final T page;
    protected final BookContext bookContext;
    protected final HttpHostExt usedProxy;
    protected final Logger logger;

    protected AbstractPageImgProcessor(final BookContext bookContext, final T page, final HttpHostExt usedProxy) {
        this.bookContext = bookContext;
        this.page = page;
        this.usedProxy = usedProxy;
        logger = ExecutionContext.INSTANCE.getLogger(getClass(), bookContext);
    }

    protected boolean processImage(final String imgUrl) {
        return processImage(imgUrl, HttpHostExt.NO_PROXY);
    }

    protected boolean processImage(final String imgUrl, final HttpHostExt proxy) {
        if (GBDOptions.secureMode() && proxy.isLocal()) return false;

        InputStream inputStream = null;
        IStoredItem storedItem = null;

        if (page.isLoadingStarted()) return false;

        try (Response resp = getContent(imgUrl, proxy, false)) {
            inputStream = null == resp ? null : resp.getContent();

            if (null == inputStream) {
                logger.info(getErrorMsg(imgUrl, proxy));
                return false;
            }

            int read, totalRead = 0;
            final byte[] bytes = new byte[dataChunk];
            boolean firstChunk = true, reloadFlag;

            while (-1 != (read = inputStream.read(bytes))) {
                if (firstChunk) {
                    final String imgFormat = Images.getImageFormat(resp);
                    if (null != imgFormat) {

                        if (page.isLoadingStarted()) return false;

                        page.setLoadingStarted(true);
                        storedItem = bookContext.getStorage().getStoredItem(page, imgFormat);

                        if (reloadFlag = storedItem.exists()) if (GBDOptions.reloadImages()) storedItem.delete();
                        else {
                            page.setDataProcessed(true);
                            return false;
                        }
                    } else break;

                    if (storedItem.exists()) break;

                    if (!proxy.isLocal())
                        logger.info(String.format("Started img %s for %s with %s Proxy", reloadFlag ? "RELOADING" : "processing", page.getPid(), proxy.toString()));
                    else
                        logger.info(String.format("Started img %s for %s without Proxy", reloadFlag ? "RELOADING" : "processing", page.getPid()));
                }

                firstChunk = false;

                totalRead += read;
                storedItem.write(bytes, read);
            }

            if (validateOutput(storedItem, getImgWidth())) {
                page.setDataProcessed(true);

                proxy.promoteProxy();

                logger.info(getSuccessMsg());
                page.setDataProcessed(true);
                page.setFileExists(true);

                return true;
            } else {
                storedItem.delete();
                return false;
            }
        } catch (SocketTimeoutException | SocketException | SSLException ste) {
            proxy.registerFailure();
        } catch (final Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != storedItem) {
                try {
                    storedItem.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            if (!page.isDataProcessed() && null != storedItem) {
                logger.info(String.format("Loading page %s failed!", page.getPid()));
                try {
                    storedItem.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    protected abstract String getErrorMsg(String imgUrl, HttpHostExt proxy);

    protected abstract String getSuccessMsg();

    @Override
    public T getUniqueObject() {
        return page;
    }

    @Override
    public String toString() {
        return "Page processor:" + bookContext;
    }

    protected static int getImgWidth() {
        return 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();
    }

    protected abstract boolean validateOutput(IStoredItem storedItem, int width);
}
