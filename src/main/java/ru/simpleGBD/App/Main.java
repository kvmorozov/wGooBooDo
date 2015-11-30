package ru.simpleGBD.App;

import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    private static final String TEST_BOOK_ID = "pfwAG3-rpzcC";

    public static void main(String[] args) {
        ImageExtractor extractor = new ImageExtractor(TEST_BOOK_ID);

        extractor.process();
    }
}
