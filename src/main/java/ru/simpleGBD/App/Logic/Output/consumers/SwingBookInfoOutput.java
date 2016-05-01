package ru.simpleGBD.App.Logic.Output.consumers;

import ru.simpleGBD.App.GUI.swing.MainBookForm;
import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Logic.Output.listeners.SwingLogEventListener;

import javax.swing.*;

/**
 * Created by km on 13.12.2015.
 */
public class SwingBookInfoOutput extends AbstractOutput {

    public SwingBookInfoOutput(MainBookForm form) {
        addListener(new SwingLogEventListener());
    }

    @Override
    public void receiveBookInfo(BookInfo bookInfo) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                MainBookForm.getINSTANCE().getTfBookTitle().setText(bookInfo.getBookData().getTitle());
            });
        }
    }
}
