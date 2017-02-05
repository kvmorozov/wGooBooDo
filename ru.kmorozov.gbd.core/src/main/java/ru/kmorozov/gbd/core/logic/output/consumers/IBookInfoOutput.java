package ru.kmorozov.gbd.core.logic.output.consumers;

import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;

/**
 * Created by km on 13.12.2015.
 */
public interface IBookInfoOutput {

    void receiveBookInfo(BookInfo bookInfo);
}
