package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import org.asynchttpclient.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 23.12.2016.
 */
public class AsyncHttpResponse implements ru.kmorozov.gbd.core.logic.connectors.Response {

    private final Response response;

    AsyncHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public InputStream getContent() throws IOException {
        return response == null ? null : response.getResponseBodyAsStream();
    }

    @Override
    public String getImageFormat() {
        String rawContendType = response.getContentType();

        return rawContendType.startsWith("image/") ? rawContendType.split("/")[1] : "badFile";
    }

    @Override
    public void close() throws IOException {

    }
}
