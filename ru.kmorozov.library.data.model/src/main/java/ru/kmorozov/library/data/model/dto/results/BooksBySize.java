package ru.kmorozov.library.data.model.dto.results;

import ru.kmorozov.library.data.model.book.BookInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 28.07.2017.
 */
public class BooksBySize {

    private List<String> bookIds;

    private Integer count;

    private Long size;

    private BookInfo.BookFormat format;

    public Collection<String> getBookIds() {
        return bookIds;
    }

    public void setBookIds(final List<String> bookIds) {
        this.bookIds = bookIds;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public BookInfo.BookFormat getFormat() {
        return format;
    }

    public void setFormat(final BookInfo.BookFormat format) {
        this.format = format;
    }
}
