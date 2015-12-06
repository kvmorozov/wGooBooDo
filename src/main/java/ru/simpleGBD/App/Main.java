package ru.simpleGBD.App;

import ru.simpleGBD.App.Config.CommandLineOptions;
import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Config.IGBDOptions;
import ru.simpleGBD.App.Config.LocalSystemOptions;
import ru.simpleGBD.App.GUI.MainFrame;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

public class Main {

    public static void main(String[] args) {
        if (args.length < 0) {

            GBDOptions.init(new CommandLineOptions(args));

            String bookId = GBDOptions.getBookId();
            if (bookId == null || bookId.length() == 0)
                return;

            (new ImageExtractor()).process();
        }
        else {
            GBDOptions.init(new LocalSystemOptions());
            (new MainFrame()).setVisible();
        }
    }
}
