package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.db.core.config.IContextLoader;
import ru.kmorozov.gbd.core.config.IIndex;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class DirContextLoader implements IContextLoader {

    public static final DirContextLoader BOOK_CTX_LOADER = new DirContextLoader();
    private final Map<String, BookInfo> booksInfo = new HashMap<>();

    public DirContextLoader() {
        this.initContext();
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

        List<BookInfo> runtimeBooksInfo = ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::getBookInfo).collect(Collectors.toList());
        for (BookInfo bookInfo : runtimeBooksInfo)
            this.booksInfo.put(bookInfo.getBookId(), bookInfo);

        this.getIndex(true).updateIndex((new ArrayList<>(this.booksInfo.values())));
    }

    @Override
    public void updateBookInfo(final BookInfo bookInfo) {
        throw new UnsupportedOperationException();
    }

    private void initContext() {
        if (!GBDOptions.isValidConfig()) return;

        this.refreshContext();
    }

    public BookInfo getBookInfo(String bookId) {
        return this.booksInfo.get(bookId);
    }

    @Override
    public Set<String> getBookIdsList() {
        return this.booksInfo.isEmpty() ? new HashSet<>() : this.booksInfo.keySet();
    }

    public Iterable<BookInfo> getBooks() {
        return this.booksInfo.values();
    }

    public int getContextSize() {
        return this.booksInfo.size();
    }

    public void refreshContext() {
        IIndex index = this.getIndex(false);
        if (null == index) return;

        IBookInfo[] ctxObjArr = index.getBooks();

        for (Object ctxObj : ctxObjArr)
            if (ctxObj instanceof BookInfo) {
                BookInfo bookInfo = (BookInfo) ctxObj;
                this.booksInfo.put(bookInfo.getBookId(), bookInfo);
            }
    }

    @Override
    public boolean isValid() {
        return GBDOptions.isValidConfig();
    }

    protected IIndex getIndex(boolean createIfNotExists) {
        if (!GBDOptions.isValidConfig()) return null;

        return GBDOptions.getStorage().getIndex(this.getLoadedFileName(), createIfNotExists);
    }
}
