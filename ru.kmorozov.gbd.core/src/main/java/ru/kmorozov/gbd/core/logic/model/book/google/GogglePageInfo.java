package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

import java.io.Serializable;

/**
 * Created by km on 21.11.2015.
 */

public class GogglePageInfo extends AbstractPage implements Serializable, Comparable<GogglePageInfo> {

    @SerializedName("pid")
    private String pid;
    @SerializedName("flags")
    private String flags;
    @SerializedName("title")
    private String title;
    private String src;
    @SerializedName("uf")
    private String uf;
    private String sig;

    @SerializedName("order")
    private int order;
    @SerializedName("h")
    private int h;
    private int width;
    @SerializedName("links")
    private Object links;

    private String prefix;
    private int pageNum = -1;

    private transient boolean gapPage = false;

    public GogglePageInfo() {
    }

    // Создание страниц для заполнения разрыва
    public GogglePageInfo(String pid, int order) {
        this.pid = pid;
        this.order = order;

        gapPage = true;
    }

    @Override
    public String getPid() {
        return pid;
    }

    @Override
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

    public boolean isSigChecked() {return sigChecked.get();}
}
