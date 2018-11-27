package ru.kmorozov.gbd.core.logic.context;

import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.gbd.core.loader.DirContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;

import java.util.Set;

/**
 * Created by sbt-morozov-kv on 02.12.2016.
 */
public class ContextProvider implements IContextLoader {

    private static final String DB_CTX_PROVIDER_CLASS_NAME = "ru.kmorozov.library.data.loader.processors.gbd.DbContextLoader";

    private static final Object LOCK_OBJ = new Object();
    private static volatile IContextLoader contextProvider;

    protected IContextLoader loader;

    public ContextProvider(final IContextLoader loader) {
        this.loader = loader;
    }

    public static IContextLoader getContextProvider() {
        if (null == ContextProvider.contextProvider) {
            synchronized (ContextProvider.LOCK_OBJ) {
                if (null == ContextProvider.contextProvider) if (ContextProvider.classExists(ContextProvider.DB_CTX_PROVIDER_CLASS_NAME)) {
                    try {
                        ContextProvider.contextProvider = (IContextLoader) Class.forName(ContextProvider.DB_CTX_PROVIDER_CLASS_NAME).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (null == ContextProvider.contextProvider || !ContextProvider.contextProvider.isValid())
                    ContextProvider.contextProvider = new ContextProvider(DirContextLoader.BOOK_CTX_LOADER);
            }

            ContextProvider.contextProvider.updateContext();
        }

        return ContextProvider.contextProvider;
    }

    public static void setDefaultContextProvider(final IContextLoader _contextProvider) {
        ContextProvider.contextProvider = _contextProvider;
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }

    @Override
    public void updateIndex() {
        this.loader.updateIndex();
    }

    @Override
    public void updateContext() {
        this.loader.updateContext();
    }

    @Override
    public void updateBookInfo(final BookInfo bookInfo) {
        this.loader.updateBookInfo(bookInfo);
    }

    @Override
    public BookInfo getBookInfo(final String bookId) {
        return this.loader.getBookInfo(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return this.loader.getBookIdsList();
    }

    @Override
    public int getContextSize() {
        return this.loader.getContextSize();
    }

    @Override
    public void refreshContext() {
        this.loader.refreshContext();
    }

    @Override
    public boolean isValid() {
        return this.loader.isValid();
    }
}
