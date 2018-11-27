package ru.kmorozov.gbd.core.logic.connectors.asynchttp;

import org.asynchttpclient.Response;

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
    public InputStream getContent() {
        return null == this.response ? null : this.response.getResponseBodyAsStream();
    }

    @Override
    public String getImageFormat() {
        String rawContendType = this.response.getContentType();

        return rawContendType.startsWith("image/") ? rawContendType.split("/")[1] : "badFile";
    }

    @Override
    public void close() {

    }
}
