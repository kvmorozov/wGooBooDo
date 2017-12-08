package com.wouterbreukink.onedrive.client.exceptions;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class InvalidCodeException extends OneDriveException {

    public InvalidCodeException(final OneDriveErrorInfo errorInfo) {
        super(errorInfo);
    }
}
