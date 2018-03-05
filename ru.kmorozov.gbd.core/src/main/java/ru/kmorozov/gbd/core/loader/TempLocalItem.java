package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;
import java.io.IOException;

public class TempLocalItem extends LocalFSStoredItem {

    public TempLocalItem(LocalFSStorage storage, IPage page, String imgFormat) {
        super(storage, page, imgFormat);
    }

    @Override
    protected void init() {
        try {
            outputFile = File.createTempFile(page.getOrder() + '_' + page.getPid(), imgFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
