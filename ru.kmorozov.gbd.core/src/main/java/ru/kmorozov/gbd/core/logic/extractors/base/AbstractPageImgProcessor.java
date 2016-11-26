package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.*;
import java.net.SocketTimeoutException;

/**
 * Created by sbt-morozov-kv on 18.11.2016.
 */
public abstract class AbstractPageImgProcessor<T extends AbstractPage> extends AbstractHttpProcessor implements IUniqueRunnable<T> {

    private static final int dataChunk = 4096;

    protected final T page;
    protected final BookContext bookContext;
    protected final HttpHostExt usedProxy;
    protected final Logger logger;

    public AbstractPageImgProcessor(BookContext bookContext, T page, HttpHostExt usedProxy) {
        this.bookContext = bookContext;
        this.page = page;
        this.usedProxy = usedProxy;
        this.logger = ExecutionContext.INSTANCE.getLogger(getClass(), bookContext);
    }

    protected boolean processImage(String imgUrl) {
        return processImage(imgUrl, HttpHostExt.NO_PROXY);
    }

    protected boolean processImage(String imgUrl, HttpHostExt proxy) {
        if (GBDOptions.secureMode() && proxy.isLocal()) return false;

        File outputFile = null;
        Response resp = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        if (page.loadingStarted.get()) return false;

        try {
            resp = getContent(imgUrl, proxy, false);
            inputStream = resp == null ? null : resp.getContent();

            if (inputStream == null) {
                logger.info(getErrorMsg(imgUrl, proxy));
                return false;
            }

            int read;
            byte[] bytes = new byte[dataChunk];
            boolean firstChunk = true, reloadFlag;

            while ((read = inputStream.read(bytes)) != -1) {
                if (firstChunk) {
                    String imgFormat = Images.getImageFormat(resp);
                    if (imgFormat != null) {

                        if (page.loadingStarted.get()) return false;

                        page.loadingStarted.set(true);
                        outputFile = new File(bookContext.getOutputDir().getPath() + "\\" + page.getOrder() + "_" + page.getPid() + "." + imgFormat);

                        if (reloadFlag = outputFile.exists()) if (GBDOptions.reloadImages()) outputFile.delete();
                        else return false;
                    } else break;

                    if (outputFile.exists()) break;

                    if (!proxy.isLocal())
                        logger.info(String.format("Started img %s for %s with %s Proxy", reloadFlag ? "RELOADING" : "processing", page.getPid(), proxy.toString()));
                    else
                        logger.info(String.format("Started img %s for %s without Proxy", reloadFlag ? "RELOADING" : "processing", page.getPid()));

                    outputStream = new FileOutputStream(outputFile);
                }

                firstChunk = false;

                outputStream.write(bytes, 0, read);
            }

            page.dataProcessed.set(true);

            proxy.promoteProxy();

            logger.info(getSuccessMsg());
            page.dataProcessed.set(true);
            page.fileExists.set(true);

            return true;
        } catch (SocketTimeoutException ste) {
            proxy.registerFailure();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!page.dataProcessed.get() && outputFile != null) {
                logger.info(String.format("Loading page %s failed!", page.getPid()));
                outputFile.delete();
            }

            try {
                if (resp != null) resp.close();
            } catch (IOException e) {
                e.printStackTrace();
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
        return "Page processor:" + bookContext.toString();
    }
}
