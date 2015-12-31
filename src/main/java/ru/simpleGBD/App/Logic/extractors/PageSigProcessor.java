package ru.simpleGBD.App.Logic.extractors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.api.client.http.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.NoHttpResponseException;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.Output.progress.ProcessStatus;
import ru.simpleGBD.App.Logic.Proxy.HttpHostExt;
import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Logic.model.book.PageInfo;
import ru.simpleGBD.App.Logic.model.book.PagesInfo;
import ru.simpleGBD.App.Utils.Mapper;
import ru.simpleGBD.App.Utils.Pools;

import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by km on 21.11.2015.
 */
public class PageSigProcessor extends AbstractHttpProcessor implements Runnable {

    private static final String SIG_ERROR_TEMPLATE = "No sig at %s";

    private HttpHostExt proxy;
    private BookInfo bookInfo;

    public PageSigProcessor(HttpHostExt proxy) {
        this.proxy = proxy;
    }

    private boolean getSig(PageInfo page) {
        if (page.dataProcessed.get() || page.getSig() != null || page.sigChecked.get())
            return false;

        boolean sigFound = false;
        String rqUrl = ExecutionContext.baseUrl + ImageExtractor.PAGES_REQUEST_TEMPLATE.replace(ImageExtractor.RQ_PG_PLACEHOLDER, page.getPid());

        try {
//            logger.finest(String.format("Started sig processing for %s with proxy %s", page.getPid(), proxy == null ? HttpHostExt.NO_PROXY : proxy.toString()));

            HttpResponse resp = getContent(rqUrl, proxy, false);
            if (resp == null) {
                logger.info(String.format(SIG_ERROR_TEMPLATE, rqUrl));
                return false;
            }

            String respStr = IOUtils.toString(resp.getContent());
            PagesInfo framePages = Mapper.objectMapper.readValue(respStr, PagesInfo.class);

            PageInfo[] pages = framePages.getPagesArray();
            for (PageInfo framePage : pages)
                if (framePage.getOrder() >= page.getOrder() && framePage.getSrc() != null) {
                    PageInfo _page = bookInfo.getPagesInfo().getPageByPid(framePage.getPid());

                    if (_page.dataProcessed.get() || _page.getSig() != null || _page.sigChecked.get())
                        continue;

                    String _frameSrc = framePage.getSrc();
                    if (_frameSrc != null)
                        _page.setSrc(_frameSrc);

                    if (_page.getSig() != null) {
                        if (_page.getPid().equals(page.getPid()))
                            sigFound = true;
                        _page.sigChecked.set(true);

                        // Если есть возможность - пытаемся грузить страницу сразу
                        Pools.imgExecutor.execute(new PageImgProcessor(_page, proxy));
                    }

                    if (_page.getSrc() != null && _page.getSig() == null)
                        logger.info(String.format(SIG_ERROR_TEMPLATE, rqUrl));
                }
        } catch (JsonParseException | JsonMappingException | SocketTimeoutException | SocketException | NoHttpResponseException ce) {
            if (proxy != null) {
                proxy.registerFailure();
                logger.info(String.format("Proxy %s failed!", proxy.toString()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //            logger.finest(String.format("Finished sig processing for %s; sig %s found.", page.getPid(), sigFound ? "" : " not "));

        return sigFound;
    }

    @Override
    public void run() {
        if (proxy != null && !proxy.isAvailable())
            return;

        bookInfo = SerializationUtils.clone(ExecutionContext.bookInfo);

        final ProcessStatus psSigs = new ProcessStatus(bookInfo.getPagesInfo().getPages().size());

        ExecutionContext.bookInfo.getPagesInfo().getPages().parallelStream()
                .forEach(page -> {
                    psSigs.inc();
                    getSig(page);
                });

        psSigs.finish();
    }
}
