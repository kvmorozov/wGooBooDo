package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by km on 26.12.2016.
 */

@Document
public class StorageInfo {

    long filesCount;
    long lastChecked;

    public StorageInfo() {
    }

    public StorageInfo(long filesCount) {
        this.filesCount = filesCount;
    }

    public long getFilesCount() {
        return this.filesCount;
    }

    public void setFilesCount(long filesCount) {
        this.filesCount = filesCount;
    }

    public void incFilesCount() {
        filesCount++;
    }

    public long getLastChecked() {
        return this.lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }
}
