package ru.kmorozov.gbd.core.logic.extractors;

import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.Proxy.AbstractProxyListProvider;
import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;
import ru.kmorozov.gbd.core.logic.connectors.Response;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.model.book.PageInfo;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;

import java.io.*;
import java.net.SocketTimeoutException;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */
class PageImgProcessor extends AbstractHttpProcessor implements Runnable {

    private static final String IMG_ERROR_TEMPLATE = "No img at %s with proxy %s";

    private static final int dataChunk = 4096;

    private final PageInfo page;
    private final HttpHostExt usedProxy;
    private final BookContext bookContext;
    private final Logger logger;

    public PageImgProcessor(BookContext bookContext, PageInfo page, HttpHostExt usedProxy) {
        this.bookContext = bookContext;
        this.page = page;
        this.usedProxy = usedProxy;
        this.logger = INSTANCE.getLogger(PageImgProcessor.class, bookContext);
    }

    private boolean processImage(String imgUrl, HttpHostExt proxy) {
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
                logger.info(String.format(IMG_ERROR_TEMPLATE, imgUrl, proxy.toString()));
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

                    if (outputFile != null && outputFile.exists()) break;

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

            logger.info(String.format("Finished img processing for %s%s", page.getPid(), page.isGapPage() ? " with gap" : ""));
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

    private boolean processImageWithProxy(HttpHostExt proxy) {
        return !(!proxy.isLocal() && !proxy.isAvailable()) && processImage(page.getImqRqUrl(bookContext.getBookInfo().getBookId(), ImageExtractor.HTTPS_TEMPLATE, GBDOptions.getImageWidth()), proxy);
    }

    @Override
    public void run() {
        if (page.dataProcessed.get()) return;

        if (!processImageWithProxy(usedProxy)) {
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (!usedProxy.isLocal()) if (!processImageWithProxy(HttpHostExt.NO_PROXY))
                // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
                AbstractProxyListProvider.getInstance().getParallelProxyStream().forEach(proxy -> {
                    if (proxy != usedProxy) if (processImageWithProxy(proxy)) {
                    }
                });
        }
    }
}