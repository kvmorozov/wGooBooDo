package ru.kmorozov.gbd.core.loader;

import ru.kmorozov.gbd.core.logic.model.book.base.IPage;

import java.io.File;

public class LocalFSStoredItem extends RawFileItem {

    protected final LocalFSStorage storage;
    protected final IPage page;
    protected final String imgFormat;

    protected LocalFSStoredItem(File file, LocalFSStorage storage, IPage page, String imgFormat) {
        super(file);

        this.storage = storage;
        this.page = page;
        this.imgFormat = imgFormat;
    }

    public LocalFSStoredItem(LocalFSStorage storage, IPage page, String imgFormat) {
        this(new File(storage.getStorageDir().getPath() + File.separator + page.getOrder() + '_' + page.getPid() + '.' + imgFormat), storage, page, imgFormat);
    }
}
