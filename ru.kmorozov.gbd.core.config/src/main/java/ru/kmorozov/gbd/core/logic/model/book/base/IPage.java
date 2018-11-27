package ru.kmorozov.gbd.core.logic.model.book.base;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public interface IPage {

    String getPid();

    Integer getOrder();

    boolean isSigChecked();

    boolean isDataProcessed();

    boolean isFileExists();

    boolean isLoadingStarted();
}
