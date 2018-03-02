package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.gbd.client.RestClient;
import ru.kmorozov.gbd.core.loader.DirContextLoader;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

/**
 * Created by sbt-morozov-kv on 15.12.2016.
 */
public class ManageController {

    private static final RestClient restClient = new RestClient();

    public static boolean isManageAllowed() {
        return restClient.serviceAvailable();
    }

    public static boolean isImportAllowed() {
        return restClient.ping();
    }

    public static void synchronize() {
        if (!restClient.serviceAvailable()) return;

        final DirContextLoader loader = new DirContextLoader();
        for (final BookInfo bookInfo : loader.getBooks());
//            restClient.synchronizeGoogleBook(bookInfo.getBookId());
    }
}
