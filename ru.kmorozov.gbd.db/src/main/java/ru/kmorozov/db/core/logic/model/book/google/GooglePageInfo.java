package ru.kmorozov.db.core.logic.model.book.google;

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
    public GooglePageInfo(String pid, int order) {
        this.pid = pid;
        this.order = order;

        this.gapPage = true;
    }

    @Override
    public String getPid() {
        return this.pid;
    }

    @Override
    public Integer getOrder() {
        return this.order;
    }

    public String getSrc() {
        return this.src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public CharSequence getSig() {
        return null == this.src ? null : null == this.sig ? this.sig = this.src.substring(this.src.indexOf("sig=") + 4) : this.sig;
    }

    public boolean isGapPage() {
        return this.gapPage;
    }

    public String getImqRqUrl(CharSequence bookId, String urlTemplate, int width) {
        return urlTemplate.replace(BOOK_ID_PLACEHOLDER, bookId) + IMG_REQUEST_TEMPLATE.replace(RQ_PG_PLACEHOLDER, this.pid)
                                                                                      .replace(RQ_SIG_PLACEHOLDER, this.getSig())
                                                                                      .replace(RQ_WIDTH_PLACEHOLDER, String
                                                                                              .valueOf(width));
    }

    public String getPrefix() {
        if (StringUtils.isEmpty(this.prefix)) this.parsePageNum();

        return this.prefix;
    }

    public int getPageNum() {
        return this.pageNum;
    }

    private void parsePageNum() {
        boolean numFound = false;
        this.prefix = "";
        StringBuilder strNum = new StringBuilder();
        for (int i = this.pid.length() - 1; 0 <= i; i--) {
            char ch = this.pid.charAt(i);
            if (!numFound && Character.isDigit(ch)) strNum.insert(0, ch);
            else if (!numFound && !Character.isDigit(ch)) {
                numFound = true;
                this.prefix = ch + this.prefix;
            }
            else this.prefix = ch + this.prefix;
        }

        try {
            this.pageNum = Integer.parseInt(strNum.toString());
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public int compareTo(GooglePageInfo anotherPage) {
        return getOrder().compareTo(anotherPage.getOrder());
    }

    @Override
    public String getImgUrl() {
        throw new UnsupportedOperationException();
    }
}
