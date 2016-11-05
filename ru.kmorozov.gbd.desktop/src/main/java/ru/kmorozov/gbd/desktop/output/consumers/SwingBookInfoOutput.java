package ru.kmorozov.gbd.desktop.output.consumers;

import ru.kmorozov.gbd.core.logic.output.consumers.AbstractOutput;
import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.logic.output.listeners.SwingLogEventListener;
import ru.kmorozov.gbd.desktop.gui.MainBookForm;

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
            SwingUtilities.invokeLater(() -> MainBookForm.getINSTANCE().getTfBookTitle().setText(bookInfo.getBookData().getTitle()));
        }
    }
}
