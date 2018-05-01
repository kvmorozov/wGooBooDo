package ru.kmorozov.gbd.core.logic.model.book.base;

public interface IBookInfo {

    IBookData getBookData();

    IPagesInfo getPages();

    String getBookId();
}
