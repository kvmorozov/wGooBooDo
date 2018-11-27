package ru.kmorozov.gbd.core.logic.connectors.http2native;

import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

public class Http2Response implements Response {

    private final HttpResponse response;
    private InputStream is;

    Http2Response(HttpResponse response) {
        this.response = response;

        if (response.body() instanceof byte[])
            is = new ByteArrayInputStream((byte[]) response.body());
    }

    @Override
    public InputStream getContent() throws IOException {
        return is;
    }

    @Override
    public String getImageFormat() {
        return response.headers().firstValue("content-type").get().split("/")[1];
    }

    @Override
    public void close() throws IOException {
        if (is != null)
            is.close();
    }
}
