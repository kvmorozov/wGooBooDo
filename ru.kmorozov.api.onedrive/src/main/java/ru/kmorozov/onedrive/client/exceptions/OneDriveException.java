package ru.kmorozov.onedrive.client.exceptions;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveException extends IOException {

    protected OneDriveErrorInfo errorInfo;

    public OneDriveException(String s) {
        super(s);
    }

    public OneDriveException(OneDriveErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String getMessage() {
        return this.errorInfo.toString();
    }
}
