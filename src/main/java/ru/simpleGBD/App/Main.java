package ru.simpleGBD.App;

import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        ImageExtractor extractor = new ImageExtractor(GBDOptions.getGBDOptions(args).getBookId());

        extractor.process();
    }
}
