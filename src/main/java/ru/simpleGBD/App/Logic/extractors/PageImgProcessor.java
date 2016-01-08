package ru.simpleGBD.App.Logic.extractors;

import com.google.api.client.http.HttpResponse;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyListProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.model.book.PageInfo;
import ru.simpleGBD.App.Utils.Images;
import ru.simpleGBD.App.Utils.Logger;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Created by km on 21.11.2015.
 */
public class PageImgProcessor extends AbstractHttpProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(ExecutionContext.output, PageImgProcessor.class.getName());

    private static final String IMG_ERROR_TEMPLATE = "No img at %s";

    private static int dataChunk = 4096;

    private PageInfo page;
    private HttpHostExt usedProxy;

    public PageImgProcessor(PageInfo page, HttpHostExt usedProxy) {
        this.page = page;
        this.usedProxy = usedProxy;
    }

    private boolean processImage(String imgUrl, HttpHostExt proxy) {
        File outputFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        PageInfo _page = ExecutionContext.bookInfo.getPagesInfo().getPageByPid(page.getPid());

        if (_page.loadingStarted.get())
            return false;

        try {
            HttpResponse resp = getContent(imgUrl, proxy, false);
            inputStream = resp == null ? null : resp.getContent();

            if (inputStream == null) {
                logger.info(String.format(IMG_ERROR_TEMPLATE, imgUrl));
                return false;
            }

            int read = 0;
            byte[] bytes = new byte[dataChunk];
            boolean firstChunk = true, reloadFlag;

            while ((read = inputStream.read(bytes)) != -1) {
                if (firstChunk) {
                    String imgFormat = Images.getImageFormat(resp);
                    if (imgFormat != null) {

                        if (_page.loadingStarted.get())
                            return false;

                        _page.loadingStarted.set(true);
                        outputFile = new File(ExecutionContext.outputDir.getPath() + "\\" + page.getOrder() + "_" + page.getPid() + "." + imgFormat);

                        if (reloadFlag = outputFile.exists())
                            if (GBDOptions.reloadImages())
                                outputFile.delete();
                            else
                                return false;
                    } else
                        break;

                    if (outputFile != null && outputFile.exists())
                        break;

                    if (proxy != null)
                        logger.info(String.format("Started img %s for %s with %s Proxy",
                                reloadFlag ? "RELOADING" : "processing", page.getPid(), proxy.toString()));
                    else
                        logger.info(String.format("Started img %s for %s without Proxy",
                                reloadFlag ? "RELOADING" : "processing", page.getPid()));

                    outputStream = new FileOutputStream(outputFile);
                }

                firstChunk = false;

                outputStream.write(bytes, 0, read);
            }

            page.dataProcessed.set(true);

            logger.info(String.format("Finished img processing for %s%s", page.getPid(), page.isGapPage() ? " with gap" : ""));
            _page.dataProcessed.set(true);
            _page.fileExists.set(true);

            return true;
        } catch (ConnectException | SocketTimeoutException ce) {
            if (proxy != null) {
                proxy.registerFailure();
                logger.info(String.format("Proxy %s failed!", proxy.toString()));
            }
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
        }

        return false;
    }

    private boolean processImageWithProxy(HttpHostExt proxy) {
        if (proxy != null && !proxy.isAvailable())
            return false;

        return processImage(page.getImqRqUrl(
                ImageExtractor.HTTPS_TEMPLATE, GBDOptions.getImageWidth()), proxy);
    }

    @Override
    public void run() {
        if (page.dataProcessed.get())
            return;

        if (!processImageWithProxy(usedProxy))
            // Пробуем скачать страницу с без прокси, если не получилось с той прокси, с помощью которой узнали sig
            if (usedProxy != null)
                processImageWithProxy(null);
        // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
        for (HttpHostExt proxy : AbstractProxyListProvider.getInstance().getProxyList())
            if (proxy != usedProxy && processImageWithProxy(proxy))
                return;
    }
}
