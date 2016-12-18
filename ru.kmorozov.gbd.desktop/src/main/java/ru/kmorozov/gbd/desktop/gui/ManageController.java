package ru.kmorozov.gbd.desktop.gui;

import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.spring.LibraryContext;

/**
 * Created by sbt-morozov-kv on 15.12.2016.
 */
public class ManageController {

    private BooksRepository booksRepository;
    private Boolean manageAllowed;

    public ManageController() {
        try {
            booksRepository = (BooksRepository) LibraryContext.LIBRARY_CONTEXT.getBean("booksRepository");
        } catch (Exception ignored) {

        }
    }

    public boolean isManageAllowed() {
        if (manageAllowed == null) try {
            Class<?> clazz = Class.forName("ru.kmorozov.library.data.repository.BooksRepository");
            manageAllowed = clazz != null;
        } catch (ClassNotFoundException e) {
            manageAllowed = false;
        }

        return manageAllowed;
    }

    public boolean isImportAllowed() {
        return booksRepository != null;
    }

    public void importBooks() {

    }
}
