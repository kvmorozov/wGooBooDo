package ru.simpleGBD.App.Logic.model.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.extractors.ImageExtractor;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 21.11.2015.
 */

public class PageInfo implements Serializable {

    @JsonProperty("pid") private String pid;
    @JsonProperty("flags") private String flags;
    @JsonProperty("title") private String title;
    private String src;
    @JsonProperty("uf") private String uf;
    private String sig;

    @JsonProperty("order") private int order;
    @JsonProperty("h") private int h;
    private int width;
    @JsonProperty("links") private Object links;

    private boolean isGapPage = false;

    public PageInfo() {}

    // Создание страниц для заполнения разрыва
    public PageInfo(String pid, int order) {
        this.pid = pid;
        this.order = order;

        isGapPage = true;
    }

    public AtomicBoolean sigChecked    = new AtomicBoolean(false);
    public AtomicBoolean dataProcessed = new AtomicBoolean(false);
    public AtomicBoolean fileExists    = new AtomicBoolean(false);

    public String getPid() {
        return pid;
    }

    public int getOrder() {
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
        return ExecutionContext.baseUrl + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLDER, getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, getSig())
                .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, String.valueOf(ImageExtractor.DEFAULT_PAGE_WIDTH));
    }

    public String getImqRqUrl(String urlTemplate, int width) {
        return urlTemplate.replace(ImageExtractor.BOOK_ID_PLACEHOLDER, ExecutionContext.bookId) + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLDER, getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLDER, getSig())
                .replace(ImageExtractor.RQ_WIDTH_PLACEHOLDER, String.valueOf(width));
    }
}
