package ru.simpleGBD.App.Logic.Output;

import ru.simpleGBD.App.GUI.MainBookForm;
import ru.simpleGBD.App.Logic.DataModel.BookInfo;

import javax.swing.*;

/**
 * Created by km on 13.12.2015.
 */
public class SwingBookInfoOutput implements IBookInfoOutput {

    private MainBookForm form;

    public SwingBookInfoOutput(MainBookForm form) {
        this.form = form;
    }

    @Override
    public void receiveBookInfo(BookInfo bookInfo) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                form.getTfBookTitle().setText(bookInfo.getBookData().getTitle());
            });
        }

    }
}
