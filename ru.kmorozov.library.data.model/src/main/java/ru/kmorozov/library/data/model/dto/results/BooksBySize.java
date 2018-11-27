package ru.kmorozov.library.data.model.dto.results;

import ru.kmorozov.library.data.model.book.BookInfo;
import ru.kmorozov.library.data.model.book.BookInfo.BookFormat;

import java.util.Collection;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 28.07.2017.
 */
public class BooksBySize {

    private List<String> bookIds;

    private Integer count;

    private Long size;

    private BookFormat format;

    public Collection<String> getBookIds() {
        return this.bookIds;
    }

    public void setBookIds(List<String> bookIds) {
        this.bookIds = bookIds;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public BookFormat getFormat() {
        return this.format;
    }

    public void setFormat(BookFormat format) {
        this.format = format;
    }
}
