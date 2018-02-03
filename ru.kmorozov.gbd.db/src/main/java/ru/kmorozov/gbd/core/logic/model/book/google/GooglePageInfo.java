package ru.kmorozov.gbd.core.logic.model.book.google;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

import java.io.Serializable;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.*;

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

    private transient boolean gapPage;

    public GooglePageInfo() {
    }

    // Создание страниц для заполнения разрыва
    public GooglePageInfo(final String pid, final int order) {
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

    public void setSrc(final String src) {
        this.src = src;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public CharSequence getSig() {
        return null == src ? null : null == sig ? sig = src.substring(src.indexOf("sig=") + 4) : sig;
    }

    public boolean isGapPage() {
        return gapPage;
    }

    public String getImqRqUrl(final CharSequence bookId, final String urlTemplate, final int width) {
        return urlTemplate.replace(BOOK_ID_PLACEHOLDER, bookId) + IMG_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, pid)
                                                                                      .replace(RQ_SIG_PLACEHOLDER, getSig())
                                                                                      .replace(RQ_WIDTH_PLACEHOLDER, String
                                                                                              .valueOf(width));
    }

    public String getPrefix() {
        if (StringUtils.isEmpty(prefix)) parsePageNum();

        return prefix;
    }

    public int getPageNum() {
        return pageNum;
    }

    private void parsePageNum() {
        boolean numFound = false;
        prefix = "";
        final StringBuilder strNum = new StringBuilder();
        for (int i = pid.length() - 1; 0 <= i; i--) {
            final char ch = pid.charAt(i);
            if (!numFound && Character.isDigit(ch)) strNum.insert(0, ch);
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true;
                prefix = ch + prefix;
            }
            else prefix = ch + prefix;
        }

        try {
            pageNum = Integer.parseInt(strNum.toString());
        } catch (final NumberFormatException ignored) {
        }
    }

    @Override
    public int compareTo(final GooglePageInfo anotherPage) {
        return this.getOrder().compareTo(anotherPage.getOrder());
    }
}
