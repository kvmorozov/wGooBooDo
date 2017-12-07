package com.wouterbreukink.onedrive.client.exceptions;

import com.wouterbreukink.onedrive.client.utils.JsonUtils;

import java.io.IOException;

/**
 * Created by sbt-morozov-kv on 13.03.2017.
 */
public class OneDriveExceptionFactory {

    public static OneDriveException getException(String content) {
        OneDriveErrorInfo errorInfo = null;

        try {
            errorInfo = JsonUtils.JSON_FACTORY.fromString(content, OneDriveErrorInfo.class);
            switch (errorInfo.error) {
                case "invalid_grant":
                case "server_error":
                    return new InvalidCodeException(errorInfo);
                default:
                    return new OneDriveException(errorInfo);
            }
        } catch (IOException ignored) {
        }
        return new OneDriveException(errorInfo);
    }
}
