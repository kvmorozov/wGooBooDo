package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.loader.BookListLoader;
import ru.kmorozov.gbd.core.loader.DirContextLoader;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public class ContextProvider implements IBaseLoader {

    private static final String DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.gbd.core.config.storage.DbContextProvider";

    private static final Object LOCK_OBJ = new Object();
    private static volatile ContextProvider contextProvider;

    protected IBaseLoader loader;

    public ContextProvider(IBaseLoader loader) {
        this.loader = loader;
    }

    public static ContextProvider getContextProvider() {
        if (null == contextProvider) {
            synchronized (LOCK_OBJ) {
                if (null == contextProvider) if (classExists(DB_CTX_PROVIDER_CLASS_NAME)) {
                    try {
                        contextProvider = (ContextProvider) Class.forName(DB_CTX_PROVIDER_CLASS_NAME).getDeclaredConstructor().newInstance();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

                if (null == contextProvider || !contextProvider.isValid())
                    contextProvider = new ContextProvider(BookListLoader.BOOK_CTX_LOADER);
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

    @Override
    public void updateIndex() {
        loader.updateIndex();
    }

    @Override
    public void updateContext() {
        loader.updateContext();
    }

    @Override
    public BookInfo getBookInfo(String bookId) {
        return loader.getBookInfo(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return loader.getBookIdsList();
    }

    @Override
    public int getContextSize() {
        return loader.getContextSize();
    }

    @Override
    public void refreshContext() {
        loader.refreshContext();
    }

    @Override
    public boolean isValid() {
        return loader.isValid();
    }
}
