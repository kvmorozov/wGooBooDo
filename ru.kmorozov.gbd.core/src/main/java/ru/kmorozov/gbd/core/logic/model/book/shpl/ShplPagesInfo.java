package ru.kmorozov.gbd.core.logic.model.book.shpl;

import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;
import ru.kmorozov.gbd.core.logic.model.book.google.GooglePageInfo;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPagesInfo implements IPagesInfo {

    @SerializedName("page")
    private ShplPage[] pages;

    public ShplPagesInfo(ShplPage[] pages) {
        this.pages = pages;
    }

    @Override
    public ShplPage[] getPages() {
        return pages;
    }

    @Override
    public String getMissingPagesList() {
        return null;
    }

    @Override
    public void build() {

    }

    @Override
    public GooglePageInfo getPageByPid(String pid) {
        return null;
    }
}
