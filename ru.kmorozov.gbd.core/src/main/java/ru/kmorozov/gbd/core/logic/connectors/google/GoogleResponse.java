package ru.kmorozov.gbd.core.logic.connectors.google;

import com.google.api.client.http.HttpResponse;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public class GoogleResponse implements Response {

    private final HttpResponse resp;

    public GoogleResponse(HttpResponse resp) {
        this.resp = resp;
    }

    @Override
    public InputStream getContent() throws IOException {
        return resp == null ? null : resp.getContent();
    }

    @Override
    public String getImageFormat() {
        return resp.getMediaType().getSubType();
    }

    @Override
    public void close() {

    }
}
