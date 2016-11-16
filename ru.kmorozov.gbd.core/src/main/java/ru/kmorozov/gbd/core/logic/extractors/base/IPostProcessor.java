package ru.kmorozov.gbd.core.logic.extractors.base;

import ru.kmorozov.gbd.core.logic.context.BookContext;

/**
 * Created by km on 09.11.2016.
 */
public interface IPostProcessor extends IUniqueRunnable<BookContext> {

    void setBookContext(BookContext bookContext);

    void make();
}
