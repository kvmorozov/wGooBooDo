package ru.kmorozov.gbd.core.logic.model.book.shpl;

import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPage implements IPage {

    @SerializedName("id")
    private String id;
    @SerializedName("w")
    private Integer width;
    @SerializedName("h")
    private Integer height;
    @SerializedName("downloadUrl")
    private String downloadUrl;

    @Override
    public String getPid() {
        return id;
    }
}
