package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public abstract class AbstractContextProvider {

    private static final String DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.library.data.storage.mongo.DbContextProvider";

    private static final Object LOCK_OBJ = new Object();
    private static volatile AbstractContextProvider contextProvider;

    public static AbstractContextProvider getContextProvider() {
        if (contextProvider == null) {
            synchronized (LOCK_OBJ) {
                if (contextProvider == null) if (classExists(DB_CTX_PROVIDER_CLASS_NAME)) {
                    try {
                        contextProvider = (AbstractContextProvider) Class.forName(DB_CTX_PROVIDER_CLASS_NAME).newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (contextProvider == null || !contextProvider.isValid()) contextProvider = new FileContextProvider();
            }
        }

        return contextProvider;
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

    public abstract void updateIndex();

    public abstract void updateContext();

    public abstract BookInfo getBookInfo(String bookId);

    public abstract List<String> getBookIdsList();

    public abstract int getContextSize();

    public abstract void refreshContext();

    public abstract boolean isValid();
}
