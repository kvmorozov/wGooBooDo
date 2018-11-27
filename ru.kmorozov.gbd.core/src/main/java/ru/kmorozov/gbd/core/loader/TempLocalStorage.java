package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.config.IStoredItem;
import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.IOException;

public class TempLocalStorage extends LocalFSStorage {

    public static final LocalFSStorage DEFAULT_TEMP_STORAGE = new TempLocalStorage();

    private TempLocalStorage() {
        super(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public IStoredItem getStoredItem(IPage page, String imgFormat) throws IOException {
        return new TempLocalItem(this, page, imgFormat);
    }
}
