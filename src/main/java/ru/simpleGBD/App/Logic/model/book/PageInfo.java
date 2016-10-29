package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import ru.simpleGBD.App.Logic.extractors.ImageExtractor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.simpleGBD.App.Logic.ExecutionContext.INSTANCE;

/**
 * Created by km on 21.11.2015.
 */

public class PageInfo implements Serializable {

    @JsonProperty("pid")
    private String pid;
    @JsonProperty("flags")
    private String flags;
    @JsonProperty("title")
    private String title;
    private String src;
    @JsonProperty("uf")
    private String uf;
    private String sig;

    @JsonProperty("order")
    private int order;
    @JsonProperty("h")
    private int h;
    private int width;
    @JsonProperty("links")
    private Object links;

    private String prefix;
    private int pageNum = -1;

    private boolean isGapPage = false;

    public PageInfo() {
    }

    // Создание страниц для заполнения разрыва
    public PageInfo(String pid, int order) {
        this.pid = pid;
        this.order = order;

        isGapPage = true;
    }

    public final AtomicBoolean sigChecked = new AtomicBoolean(false);
    public final AtomicBoolean dataProcessed = new AtomicBoolean(false);
    public final AtomicBoolean fileExists = new AtomicBoolean(false);
    public final AtomicBoolean loadingStarted = new AtomicBoolean(false);

    public String getPid() {
        return pid;
    }

    public Integer getOrder() {
        return order;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getSig() {
        return src == null ? null : sig == null ? sig = src.substring(src.indexOf("sig=") + 4) : sig;
    }

    public boolean isGapPage() {
        return isGapPage;
    }

    public String getPageUrl() {
        return INSTANCE.getBaseUrl() + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLDER, getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, getSig())
                .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, String.valueOf(ImageExtractor.DEFAULT_PAGE_WIDTH));
    }

    public String getImqRqUrl(String urlTemplate, int width) {
        return urlTemplate.replace(ImageExtractor.BOOK_ID_PLACEHOLDER, INSTANCE.getBookId()) + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLDER, getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, getSig())
                .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, String.valueOf(width));
    }

    public String getPrefix() {
        if (Strings.isNullOrEmpty(prefix))
            parsePageNum();

        return prefix;
    }

    public int getPageNum() {
        return pageNum;
    }

    private void parsePageNum() {
        boolean numFound = false;
        prefix = "";
        String strNum = "";
        for (int i = pid.length() - 1; i >= 0; i--) {
            char ch = pid.charAt(i);
            if (!numFound && Character.isDigit(ch))
                strNum = ch + strNum;
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true;
                prefix = ch + prefix;
            }
            else
                prefix = ch + prefix;
        }

        try {
            pageNum = Integer.parseInt(strNum);
        } catch (NumberFormatException ignored) {
        }
    }
}
