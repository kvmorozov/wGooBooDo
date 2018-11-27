package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;
import java.io.IOException;

public class TempLocalItem extends LocalFSStoredItem {

    public TempLocalItem(final LocalFSStorage storage, final IPage page, final String imgFormat) {
        super(storage, page, imgFormat);
    }

    @Override
    protected void init() {
        try {
            this.outputFile = File.createTempFile(this.page.getOrder() + '_' + this.page.getPid(), this.imgFormat);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
