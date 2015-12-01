package ru.simpleGBD.App;

import ru.simpleGBD.App.Config.GBDOptions;
import ru.simpleGBD.App.Logic.Runtime.ImageExtractor;

public class Main {

    public static void main(String[] args) {
        ImageExtractor extractor = new ImageExtractor(GBDOptions.getGBDOptions(args).getBookId());

        extractor.process();
    }
}
