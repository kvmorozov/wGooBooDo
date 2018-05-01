package ru.kmorozov.gbd.core.config;

import ru.kmorozov.gbd.core.logic.model.book.base.IBookInfo;

import java.util.List;

public interface IIndex {

    IBookInfo[] getBooks();

    void updateIndex(List<IBookInfo> books);
}
