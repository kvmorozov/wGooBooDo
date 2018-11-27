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

    public DuplicatedBookDTO(BooksBySize duplicatedBook) {
        count = duplicatedBook.getCount();
        format = duplicatedBook.getFormat().name();
        size = duplicatedBook.getSize();
    }

    public List<BookDTO> getBooks() {
        return this.books;
    }

    public void setBooks(List<BookDTO> books) {
        this.books = books;
    }
}
