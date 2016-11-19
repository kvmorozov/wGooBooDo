package ru.kmorozov.gbd.core.logic.model.book.shpl;

import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.model.book.base.AbstractPage;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPage extends AbstractPage {

    @SerializedName("id")
    private String id;
    @SerializedName("w")
    private Integer width;
    @SerializedName("h")
    private Integer height;
    @SerializedName("downloadUrl")
    private String downloadUrl;

    private Integer order;

    public ShplPage() {
    }

    public ShplPage(Integer order) {
        this.order = order;
    }

    @Override
    public String getPid() {
        return id;
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    public String getImgUrl() {
        return String.format("http://elib.shpl.ru/pages/%s/zooms/%s", id, 7);
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}