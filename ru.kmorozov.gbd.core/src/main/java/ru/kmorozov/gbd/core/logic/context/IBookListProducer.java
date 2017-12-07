package ru.kmorozov.gbd.core.logic.context;

import java.util.Set;

/**
 * Created by km on 12.11.2016.
 */
@FunctionalInterface
public interface IBookListProducer {

    Set<String> getBookIds();
}
