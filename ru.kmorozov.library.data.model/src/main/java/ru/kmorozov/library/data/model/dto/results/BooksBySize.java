package ru.kmorozov.library.data.model.dto.results;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 28.07.2017.
 */
public class BooksBySize {

    private List<String> bookIds;

    private Integer count;

    private Long size;

    private String format;

    public List<String> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<String> bookIds) {
        this.bookIds = bookIds;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
