package ru.kmorozov.gbd.core.loader;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IBaseLoader;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.utils.Mapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sbt-morozov-kv on 14.11.2016.
 */
public class DirContextLoader implements IBaseLoader {

    static final DirContextLoader BOOK_CTX_LOADER = new DirContextLoader();
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

    public void updateContext() {
        if (!StringUtils.isEmpty(GBDOptions.getBookId())) return;

        final List<BookInfo> runtimeBooksInfo = ExecutionContext.INSTANCE.getContexts(false).stream().map(BookContext::getBookInfo).collect(Collectors.toList());
        for (final BookInfo bookInfo : runtimeBooksInfo)
            booksInfo.put(bookInfo.getBookId(), bookInfo);

        try {
            try (FileWriter writer = new FileWriter(getFileToLoad(true))) {
                Mapper.getGson().toJson(new ArrayList<>(booksInfo.values()), writer);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
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
        return null;
    }

    public Iterable<BookInfo> getBooks() {
        return booksInfo.values();
    }

    public int getContextSize() {
        return booksInfo.size();
    }

    public void refreshContext() {
        final File contextFile = getFileToLoad(false);
        if (null == contextFile) return;

        try {
            final BookInfo[] ctxObjArr;
            try (FileReader reader = new FileReader(contextFile)) {
                ctxObjArr = Mapper.getGson().fromJson(reader, BookInfo[].class);
            }
            for (final Object ctxObj : ctxObjArr)
                if (ctxObj instanceof BookInfo) {
                    final BookInfo bookInfo = (BookInfo) ctxObj;
                    booksInfo.put(bookInfo.getBookId(), bookInfo);
                }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        return GBDOptions.isValidConfig();
    }

    protected File getFileToLoad(final boolean createIfNotExists) {
        if (!GBDOptions.isValidConfig()) return null;

        final File indexFile = new File(GBDOptions.getOutputDir() + File.separator + getLoadedFileName());
        if (indexFile.exists()) return indexFile;
        else if (createIfNotExists) {
            try {
                indexFile.createNewFile();
                return indexFile;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
