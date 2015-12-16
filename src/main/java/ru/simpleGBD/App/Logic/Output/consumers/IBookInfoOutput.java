package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.Logic.DataModel.BookInfo;

/**
 * Created by km on 13.12.2015.
 */
public interface IBookInfoOutput {

    void receiveBookInfo(BookInfo bookInfo);
}
