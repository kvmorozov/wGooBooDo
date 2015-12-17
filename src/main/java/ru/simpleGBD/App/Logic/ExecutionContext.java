package ru.simpleGBD.App.Logic;

import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Logic.Output.consumers.AbstractOutput;

import java.io.File;

/**
 * Created by km on 22.11.2015.
 */
public class ExecutionContext {

    public static String bookId;
    public static String baseUrl;
    public static BookInfo bookInfo;
    public static File outputDir;
    public static AbstractOutput output;
}
