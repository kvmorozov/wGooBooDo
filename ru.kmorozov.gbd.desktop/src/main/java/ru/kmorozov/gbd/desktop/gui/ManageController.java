package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.gbd.client.RestClient;
import ru.kmorozov.gbd.core.config.storage.BookContextLoader;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

/**
 * Created by sbt-morozov-kv on 15.12.2016.
 */
public class ManageController {

    private static final RestClient restClient = new RestClient();

    public boolean isManageAllowed() {
        return restClient.serviceAvailable();
    }

    public boolean isImportAllowed() {
        return restClient.ping();
    }

    public void synchronize() {
        if (!restClient.serviceAvailable()) return;

        BookContextLoader loader = new BookContextLoader();
        for (BookInfo bookInfo : loader.getBooks())
            restClient.synchronizeGoogleBook(bookInfo.getBookId());
    }
}
