package ru.kmorozov.gbd.core.logic.extractors;

import ru.kmorozov.gbd.core.logic.context.BookContext;

/**
 * Created by km on 09.11.2016.
 */
public interface IPostProcessor {

    void make(BookContext bookContext, boolean dataChanged);
}
