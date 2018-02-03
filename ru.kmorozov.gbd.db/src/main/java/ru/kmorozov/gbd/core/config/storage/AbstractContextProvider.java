package ru.kmorozov.gbd.core.config.storage;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public abstract class AbstractContextProvider {

    private static final String DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.gbd.core.config.storage.DbContextProvider";

    private static final Object LOCK_OBJ = new Object();
    private static volatile AbstractContextProvider contextProvider;

    public static AbstractContextProvider getContextProvider() {
        if (null == contextProvider) {
            synchronized (LOCK_OBJ) {
                if (null == contextProvider) if (classExists(DB_CTX_PROVIDER_CLASS_NAME)) {
                    try {
                        contextProvider = (AbstractContextProvider) Class.forName(DB_CTX_PROVIDER_CLASS_NAME).getDeclaredConstructor().newInstance();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

                if (null == contextProvider || !contextProvider.isValid()) contextProvider = new FileContextProvider();
            }
        }

        return contextProvider;
    }

    private static boolean classExists(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException cnfe) {
            return false;
        }
    }

    public abstract void updateIndex();

    public abstract void updateContext();

    public abstract BookInfo getBookInfo(String bookId);

    public abstract Set<String> getBookIdsList();

    public abstract int getContextSize();

    public abstract void refreshContext();

    public abstract boolean isValid();
}
