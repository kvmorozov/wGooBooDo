package ru.kmorozov.App.Logic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by km on 21.11.2015.
 */
public class BookProcessor {

    private static Logger logger = Logger.getLogger(BookProcessor.class.getName());

    private static final String OPEN_PAGE_ADD_URL = "&printsec=frontcover&hl=ru#v=onepage&q&f=false";

    private String bookAddress;

    public BookProcessor(String bookAddress) {
        this.bookAddress = bookAddress;
    }

    public boolean validate() {
        try {
            URL bookUrl = new URL(bookAddress);
            URLConnection connection = bookUrl.openConnection();

            return true;
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Invalid address!");
            return false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot open url!");
            return false;
        }
    }

    public String getBookAddress() {
        return bookAddress + OPEN_PAGE_ADD_URL;
    }
}
