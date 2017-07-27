package ru.kmorozov.library.data.model.dto;

import ru.kmorozov.library.data.model.book.DuplicatedBook;

/**
 * Created by sbt-morozov-kv on 27.07.2017.
 */
public class DuplicatedBookDTO {

    private String id;
    private BookDTO book1, book2;

    public DuplicatedBookDTO() {
    }

    public DuplicatedBookDTO(DuplicatedBook duplicatedBook) {
        this.id = duplicatedBook.getDuplicateId();
        this.book1 = new BookDTO(duplicatedBook.getBook1(), false);
        this.book2 = new BookDTO(duplicatedBook.getBook2(), false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BookDTO getBook1() {
        return book1;
    }

    public void setBook1(BookDTO book1) {
        this.book1 = book1;
    }

    public BookDTO getBook2() {
        return book2;
    }

    public void setBook2(BookDTO book2) {
        this.book2 = book2;
    }
}
