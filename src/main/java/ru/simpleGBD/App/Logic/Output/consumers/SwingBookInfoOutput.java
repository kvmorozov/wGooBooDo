package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.GUI.MainBookForm;
import ru.simpleGBD.App.Logic.DataModel.BookInfo;
import ru.simpleGBD.App.Logic.Output.listeners.SwingLogEventListener;

import javax.swing.*;

/**
 * Created by km on 13.12.2015.
 */
public class SwingBookInfoOutput extends AbstractOutput {

    private MainBookForm form;

    public SwingBookInfoOutput(MainBookForm form) {
        this.form = form;

        addListener(new SwingLogEventListener());
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
