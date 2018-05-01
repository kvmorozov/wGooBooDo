package ru.kmorozov.gbd.core.logic.model.book.shpl;

import com.google.gson.annotations.SerializedName;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;
import ru.kmorozov.gbd.core.logic.model.book.base.IPagesInfo;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplPagesInfo implements IPagesInfo {

    @SerializedName("page")
    private final ShplPage[] pages;

    public ShplPagesInfo(final ShplPage[] pages) {
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
    public IPage getPageByPid(final String pid) {
        return null;
    }
}
