package ru.simpleGBD.App;

import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

public class Main {

    public static void main(String[] args) {
        String bookId = GBDOptions.getGBDOptions(args).getBookId();
        if (bookId == null || bookId.length() == 0)
            return;

        ImageExtractor extractor = new ImageExtractor(bookId);

        extractor.process();
    }
}
