package ru.kmorozov.gbd.core.logic.extractors;

/**
 * Created by km on 13.11.2016.
 */
public interface IUniqueRunnable<T> extends Runnable {

    T getUniqueObject();
}
