package ru.kmorozov.gbd.core.logic.model.book.google;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 21.11.2015.
 */

public class GogglePageInfo implements Serializable, Comparable<GogglePageInfo> {

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

    @JsonIgnore
    private boolean gapPage = false;

    public GogglePageInfo() {
    }

    // Создание страниц для заполнения разрыва
    public GogglePageInfo(String pid, int order) {
        this.pid = pid;
        this.order = order;

        gapPage = true;
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
        return gapPage;
    }

    public String getImqRqUrl(String bookId, String urlTemplate, int width) {
        return urlTemplate.replace(GoogleImageExtractor.BOOK_ID_PLACEHOLDER, bookId) + GoogleImageExtractor.IMG_REQUEST_TEMPLATE.replace(GoogleImageExtractor.RQ_PG_PLACEHOLDER, getPid()).replace(GoogleImageExtractor.RQ_SIG_PLACEHOLDER, getSig()).replace(GoogleImageExtractor.RQ_WIDTH_PLACEHOLDER, String.valueOf(width));
    }

    public String getPrefix() {
        if (Strings.isNullOrEmpty(prefix)) parsePageNum();

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
            if (!numFound && Character.isDigit(ch)) strNum = ch + strNum;
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true;
                prefix = ch + prefix;
            } else prefix = ch + prefix;
        }

        try {
            pageNum = Integer.parseInt(strNum);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public int compareTo(GogglePageInfo anotherPage) {
        return this.getOrder().compareTo(anotherPage.getOrder());
    }
}
