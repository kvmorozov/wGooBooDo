package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.logic.Proxy.HttpHostExt;

/**
 * Created by sbt-morozov-kv on 15.11.2016.
 */
public interface IImageExtractor extends Runnable {

    void newProxyEvent(HttpHostExt proxy);
}
