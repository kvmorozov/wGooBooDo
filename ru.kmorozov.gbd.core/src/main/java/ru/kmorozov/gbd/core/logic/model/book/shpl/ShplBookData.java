package ru.kmorozov.gbd.core.logic.model.book.shpl;

import ru.kmorozov.gbd.core.logic.model.book.base.IBookData;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplBookData implements IBookData {

    private final String title;

    public ShplBookData(String title) {
        this.title = title.trim();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getVolumeId() {
        return String.valueOf(title.hashCode());
    }
}
