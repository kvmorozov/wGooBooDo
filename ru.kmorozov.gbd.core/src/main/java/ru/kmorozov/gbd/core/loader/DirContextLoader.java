package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.db.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.db.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class DirContextLoader implements IBaseLoader {

    public static final DirContextLoader BOOK_CTX_LOADER = new DirContextLoader();
    private final Map<String, BookInfo> booksInfo = new HashMap<>();

    public DirContextLoader() {
        initContext();
    }

    protected String getLoadedFileName() {
        return GBDOptions.getCtxOptions().getConnectionParams();
    }

    @Override
    public void updateIndex() {

    }

    @Override
    public void updateContext() {
        if (!StringUtils.isEmpty(GBDOptions.getBookId())) return;

        if (ExecutionContext.INSTANCE == null)
            return;

        final List<BookInfo> runtimeBooksInfo = ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::getBookInfo).collect(Collectors.toList());
        for (final BookInfo bookInfo : runtimeBooksInfo)
            booksInfo.put(bookInfo.getBookId(), bookInfo);

        getIndex(true).updateIndex((new ArrayList<>(booksInfo.values())));
    }

    @Override
    public void updateBookInfo(BookInfo bookInfo) {
        throw new UnsupportedOperationException();
    }

    private void initContext() {
        if (!GBDOptions.isValidConfig()) return;

        refreshContext();
    }

    public BookInfo getBookInfo(final String bookId) {
        return booksInfo.get(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return booksInfo.isEmpty() ? new HashSet<>() : booksInfo.keySet();
    }

    public Iterable<BookInfo> getBooks() {
        return booksInfo.values();
    }

    public int getContextSize() {
        return booksInfo.size();
    }

    public void refreshContext() {
        final IIndex index = getIndex(false);
        if (null == index) return;

        final IBookInfo[] ctxObjArr = index.getBooks();

        for (final Object ctxObj : ctxObjArr)
            if (ctxObj instanceof BookInfo) {
                final BookInfo bookInfo = (BookInfo) ctxObj;
                booksInfo.put(bookInfo.getBookId(), bookInfo);
            }
    }

    @Override
    public boolean isValid() {
        return GBDOptions.isValidConfig();
    }

    protected IIndex getIndex(final boolean createIfNotExists) {
        if (!GBDOptions.isValidConfig()) return null;

        return GBDOptions.getStorage().getIndex(getLoadedFileName(), createIfNotExists);
    }
}
