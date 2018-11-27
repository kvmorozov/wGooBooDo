package ru.kmorozov.gbd.core.logic.connectors.http2native;

import ru.kmorozov.gbd.core.logic.connectors.ResponseException;

import java.io.IOException;

public class Http2ResponseException extends ResponseException {

    Http2ResponseException(final IOException ioe) {
        super(ioe);
    }

    @Override
    public int getStatusCode() {
        return 500;
    }
}
