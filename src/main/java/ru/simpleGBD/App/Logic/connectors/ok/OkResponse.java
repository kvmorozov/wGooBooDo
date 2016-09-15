package ru.simpleGBD.App.Logic.connectors.ok;

import ru.simpleGBD.App.Logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public class OkResponse implements Response {

    private final okhttp3.Response resp;

    public OkResponse(okhttp3.Response resp) {
        this.resp = resp;
    }

    @Override
    public InputStream getContent() throws IOException {
        return resp.body().byteStream();
    }

    @Override
    public String getImageFormat() {
        return resp.body().contentType().subtype();
    }

    @Override
    public void close() {
        resp.body().close();
    }
}
