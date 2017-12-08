package ru.kmorozov.library.data.model.dto.results;

import java.util.Collection;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 28.07.2017.
 */
public class BooksBySize {

    private List<String> bookIds;

    private Integer count;

    private Long size;

    private String format;

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

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }
}
