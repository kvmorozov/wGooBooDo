package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

import java.io.Serializable;

/**
 * Created by km on 21.11.2015.
 */

public class GooglePageInfo extends AbstractPage implements Serializable, Comparable<GooglePageInfo> {

    @SerializedName("pid")
    private String pid;
    @SerializedName("flags")
    private String flags;
    @SerializedName("title")
    private String title;
    private String src;
    @SerializedName("uf")
    private String uf;
    private transient String sig;

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

    public GooglePageInfo() {
    }

    // Создание страниц для заполнения разрыва
    public GooglePageInfo(String pid, int order) {
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
        return urlTemplate.replace(GoogleImageExtractor.BOOK_ID_PLACEHOLDER, bookId) + GoogleImageExtractor.IMG_REQUEST_TEMPLATE.replace(GoogleImageExtractor.RQ_PG_PLACEHOLDER, getPid())
                                                                                                                                .replace(GoogleImageExtractor.RQ_SIG_PLACEHOLDER, getSig())
                                                                                                                                .replace(GoogleImageExtractor.RQ_WIDTH_PLACEHOLDER, String
                                                                                                                                        .valueOf(width));
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
        StringBuilder strNum = new StringBuilder();
        for (int i = pid.length() - 1; i >= 0; i--) {
            char ch = pid.charAt(i);
            if (!numFound && Character.isDigit(ch)) strNum.insert(0, ch);
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true;
                prefix = ch + prefix;
            }
            else prefix = ch + prefix;
        }

        try {
            pageNum = Integer.parseInt(strNum.toString());
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public int compareTo(GooglePageInfo anotherPage) {
        return this.getOrder().compareTo(anotherPage.getOrder());
    }
}
