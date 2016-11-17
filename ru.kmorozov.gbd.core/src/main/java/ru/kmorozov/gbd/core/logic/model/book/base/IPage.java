package ru.kmorozov.gbd.core.logic.model.book.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sbt-morozov-kv on 17.11.2016.
 */
public interface IPage {

    AtomicBoolean sigChecked = new AtomicBoolean(false);
    AtomicBoolean dataProcessed = new AtomicBoolean(false);
    AtomicBoolean fileExists = new AtomicBoolean(false);
    AtomicBoolean loadingStarted = new AtomicBoolean(false);

    String getPid();
}
