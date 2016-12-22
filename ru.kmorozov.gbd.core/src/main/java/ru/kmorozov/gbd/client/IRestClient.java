package ru.kmorozov.gbd.client;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

/**
 * Created by km on 19.12.2016.
 */
public interface IRestClient {

    boolean ping();

    boolean synchronizeGoogleBook(String bookId);
}
