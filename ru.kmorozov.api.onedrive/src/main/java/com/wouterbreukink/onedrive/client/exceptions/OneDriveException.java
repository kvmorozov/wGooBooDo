package com.wouterbreukink.onedrive.client.exceptions;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveException extends IOException {

    protected OneDriveErrorInfo errorInfo;

    public OneDriveException(OneDriveErrorInfo errorInfo) {
        super();
        this.errorInfo = errorInfo;
    }
}
