package ru.simpleGBD.App.Logic.Runtime;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import ru.simpleGBD.App.Logic.DataModel.PageInfo;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Proxy.AbstractProxyPistProvider;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Utils.HttpConnections;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class PageImgProcessor implements Runnable {

    private static Logger logger = Logger.getLogger(PageImgProcessor.class.getName());
    private static byte[] pngFormat = {(byte) 0x89, 0x50, 0x4e, 0x47};
    private static int dataChunk = 4096;

    private PageInfo page;

    public PageImgProcessor(PageInfo page) {
        this.page = page;
    }

    private boolean processImage(String imgUrl, HttpClient instance, HttpHostExt proxy) {
        File outputFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            HttpResponse response = instance.execute(new HttpGet(imgUrl));

            inputStream = response.getEntity().getContent();

            int read = 0;
            byte[] bytes = new byte[dataChunk];
            boolean firstChunk = true, isPng = false;

            while ((read = inputStream.read(bytes)) != -1) {
                if (firstChunk) {
                    if (bytes[0] == pngFormat[0] && bytes[1] == pngFormat[1] && bytes[2] == pngFormat[2] && bytes[3] == pngFormat[3]) {
                        outputFile = new File(ExecutionContext.outputDir.getPath() + "\\" + page.getOrder() + "_" + page.getPid() + ".png");
                        isPng = true;
                    } else
                        break;

                    if (outputFile != null && outputFile.exists())
                        break;
                    else
                        page.dataProcessed.set(isPng);

                    if (proxy != null)
                        logger.info(String.format("Started img processing for %s with %s Proxy", page.getPid(), proxy.toString()));
                    else
                        logger.info(String.format("Started img processing for %s without Proxy", page.getPid()));

                    outputStream = new FileOutputStream(outputFile);
                }

                firstChunk = false;

                outputStream.write(bytes, 0, read);
            }

            if (page.dataProcessed.get())
                logger.info(String.format("Finished img processing for %s", page.getPid()));

            return isPng;
        } catch (ConnectException | SocketTimeoutException ce) {
            if (proxy != null)
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

            if (instance instanceof Closeable)
                try {
                    ((Closeable) instance).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return false;
    }

    private boolean processImageWithProxy(HttpHostExt proxy) {
        if (proxy != null && proxy.isAvailable())
            return false;

        if (processImage(page.getImqRqUrl(
                ImageExtractor.HTTP_TEMPLATE, ImageExtractor.DEFAULT_PAGE_WIDTH),
                HttpConnections.INSTANCE.getClient(proxy), proxy))
            return true;
        else
            return processImage(page.getImqRqUrl(
                    ImageExtractor.HTTPS_TEMPLATE, ImageExtractor.DEFAULT_PAGE_WIDTH),
                    HttpConnections.INSTANCE.getClient(proxy), proxy);
    }

    @Override
    public void run() {
        // Залочено по ошибке, разлочиваем
        if (page.getSig() == null || !page.imgRequestLock.tryLock()) {
            try {
                page.imgRequestLock.unlock();
            } catch (Exception ex) {
                return;
            }
            return;
        }

        if (!processImageWithProxy(page.getUsedProxy()))
            // Пробуем скачать страницу с другими прокси, если не получилось с той, с помощью которой узнали sig
            for (HttpHostExt proxy : AbstractProxyPistProvider.getInstance().getProxyList())
                if (processImageWithProxy(proxy))
                    return;
    }
}
