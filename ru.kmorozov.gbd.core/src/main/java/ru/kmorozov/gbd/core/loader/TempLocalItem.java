package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;
import java.io.IOException;

public class TempLocalItem extends LocalFSStoredItem {

    public TempLocalItem(LocalFSStorage storage, IPage page, String imgFormat) throws IOException {
        super(File.createTempFile(page.getOrder() + '_' + page.getPid(), imgFormat), storage, page, imgFormat);
    }
}
