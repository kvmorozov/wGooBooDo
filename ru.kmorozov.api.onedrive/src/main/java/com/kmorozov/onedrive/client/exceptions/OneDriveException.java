package com.kmorozov.onedrive.client.exceptions;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveException extends IOException {

    protected OneDriveErrorInfo errorInfo;

    public OneDriveException(final String s) {
        super(s);
    }

    public OneDriveException(final OneDriveErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String getMessage() {
        return errorInfo.toString();
    }
}
