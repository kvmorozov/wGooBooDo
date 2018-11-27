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

    protected AbstractPageImgProcessor(BookContext bookContext, T page, HttpHostExt usedProxy) {
        this.bookContext = bookContext;
        this.page = page;
        this.usedProxy = usedProxy;
        this.logger = ExecutionContext.INSTANCE.getLogger(this.getClass(), bookContext);
    }

    protected boolean processImage(String imgUrl) {
        return this.processImage(imgUrl, HttpHostExt.NO_PROXY);
    }

    protected boolean processImage(String imgUrl, HttpHostExt proxy) {
        if (GBDOptions.secureMode() && proxy.isLocal()) return false;

        InputStream inputStream = null;
        IStoredItem storedItem = null;

        if (this.page.isLoadingStarted()) return false;

        try (final Response resp = AbstractHttpProcessor.getContent(imgUrl, proxy, false)) {
            inputStream = null == resp ? null : resp.getContent();

            if (null == inputStream) {
                this.logger.info(this.getErrorMsg(imgUrl, proxy));
                return false;
            }

            int read;
            byte[] bytes = new byte[AbstractPageImgProcessor.dataChunk];
            boolean firstChunk = true, reloadFlag;

            while (-1 != (read = inputStream.read(bytes))) {
                if (firstChunk) {
                    String imgFormat = Images.getImageFormat(resp);
                    if (null != imgFormat) {

                        if (this.page.isLoadingStarted()) return false;

                        this.page.setLoadingStarted(true);
                        storedItem = this.bookContext.getStorage().getStoredItem(this.page, imgFormat);

                        if (reloadFlag = storedItem.exists()) if (GBDOptions.reloadImages()) storedItem.delete();
                        else {
                            this.page.setDataProcessed(true);
                            return false;
                        }
                    } else break;

                    if (storedItem.exists()) break;

                    if (!proxy.isLocal())
                        this.logger.info(String.format("Started img %s for %s with %s Proxy", reloadFlag ? "RELOADING" : "processing", this.page.getPid(), proxy.toString()));
                    else
                        this.logger.info(String.format("Started img %s for %s without Proxy", reloadFlag ? "RELOADING" : "processing", this.page.getPid()));
                }

                firstChunk = false;

                storedItem.write(bytes, read);
            }

            if (this.validateOutput(storedItem, AbstractPageImgProcessor.getImgWidth())) {
                this.page.setDataProcessed(true);

                proxy.promoteProxy();

                this.logger.info(this.getSuccessMsg());
                this.page.setDataProcessed(true);
                this.page.setFileExists(true);

                return true;
            } else {
                storedItem.delete();
                return false;
            }
        } catch (final SocketTimeoutException | SocketException | SSLException ste) {
            proxy.registerFailure();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != storedItem) {
                try {
                    storedItem.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!this.page.isDataProcessed() && null != storedItem) {
                this.logger.info(String.format("Loading page %s failed!", this.page.getPid()));
                try {
                    storedItem.delete();
                } catch (final IOException e) {
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
        return this.page;
    }

    @Override
    public String toString() {
        return "Page processor:" + this.bookContext;
    }

    protected static int getImgWidth() {
        return 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();
    }

    protected abstract boolean validateOutput(IStoredItem storedItem, int width);
}
