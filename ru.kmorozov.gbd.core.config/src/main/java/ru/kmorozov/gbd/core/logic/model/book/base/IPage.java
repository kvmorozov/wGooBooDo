package ru.kmorozov.gbd.core.logic.model.book.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public interface IPage {

    String getPid();

    Integer getOrder();

    public boolean isSigChecked();

    public boolean isDataProcessed();

    public boolean isFileExists();

    public boolean isLoadingStarted();
}
