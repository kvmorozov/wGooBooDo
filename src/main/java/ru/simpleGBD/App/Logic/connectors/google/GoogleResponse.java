package ru.simpleGBD.App.Logic.connectors.google;

import com.google.api.client.http.HttpResponse;
import ru.simpleGBD.App.Logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public class GoogleResponse implements Response {

    private HttpResponse resp;

    public GoogleResponse (HttpResponse resp) {
        this.resp = resp;
    }

    @Override
    public InputStream getContent() throws IOException {
        return resp.getContent();
    }

    @Override
    public String getImageFormat() {
        return resp.getMediaType().getSubType();
    }

    @Override
    public void close() {

    }
}
