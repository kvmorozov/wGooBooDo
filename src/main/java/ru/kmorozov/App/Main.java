package ru.kmorozov.App;

import ru.kmorozov.App.Logic.Runtime.ImageExtractor;

public class Main {

    private static final String TEST_BOOK_URL = "https://books.google.ru/books?id=BEvEV9OVzacC";

    public static void main(String[] args) {
        ImageExtractor extractor = new ImageExtractor(TEST_BOOK_URL);

        extractor.process();
    }
}
