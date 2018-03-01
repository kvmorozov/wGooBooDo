package com.kmorozov.onedrive.client;

import java.io.IOException;

public class OneDriveAPIException extends IOException {

    private final int code;

    public OneDriveAPIException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    public OneDriveAPIException(final int code, final String message, final Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
