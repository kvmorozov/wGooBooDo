package com.wouterbreukink.onedrive.client.exceptions;

import java.io.IOException;

import static com.wouterbreukink.onedrive.client.utils.JsonUtils.JSON_FACTORY;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveExceptionFactory {

    public static OneDriveException getException(String content) {
        OneDriveErrorInfo errorInfo = null;

        try {
            errorInfo = JSON_FACTORY.fromString(content, OneDriveErrorInfo.class);
            switch (errorInfo.error) {
                case "invalid_grant":
                    return new CodeExpiredException(errorInfo);
                default:
                    return new OneDriveException(errorInfo);
            }
        } catch (IOException ignored) {
        }
        return new OneDriveException(errorInfo);
    }
}
