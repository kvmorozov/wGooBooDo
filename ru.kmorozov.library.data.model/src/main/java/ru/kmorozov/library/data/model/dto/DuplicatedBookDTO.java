package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.dto.results.BooksBySize;

import java.util.List;

/**
 * Created by sbt-morozov-kv on 27.07.2017.
 */
public class DuplicatedBookDTO {

    private List<BookDTO> books;

    private Integer count;

    private Long size;

    private String format;

    public DuplicatedBookDTO() {
    }

    public DuplicatedBookDTO(final BooksBySize duplicatedBook) {
        this.count = duplicatedBook.getCount();
        this.format = duplicatedBook.getFormat();
        this.size = duplicatedBook.getSize();
    }

    public List<BookDTO> getBooks() {
        return books;
    }

    public void setBooks(final List<BookDTO> books) {
        this.books = books;
    }
}
