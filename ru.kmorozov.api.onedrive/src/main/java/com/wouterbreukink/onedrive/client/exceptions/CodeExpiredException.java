package com.wouterbreukink.onedrive.client.exceptions;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class CodeExpiredException extends OneDriveException {

    public CodeExpiredException(OneDriveErrorInfo errorInfo) {
        super(errorInfo);
    }
}
