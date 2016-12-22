package ru.kmorozov.gbd.client;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

/**
 * Created by km on 20.12.2016.
 */
public class RestClient extends AbstractRestClient implements IRestClient {

    @Override
    public boolean ping() {
        return getCallResult("ping", Boolean.class);
    }

    @Override
    public boolean synchronizeGoogleBook(String bookId) {
        return getCallResult("synchronizeGoogleBook", Boolean.class, new RestParam("bookId", bookId));
    }
}
