package ru.kmorozov.gbd.core.logic.connectors.ok;

import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.InputStream;

/**
 * Created by km on 17.05.2016.
 */
public class OkResponse implements Response {

    private final okhttp3.Response resp;

    OkResponse(okhttp3.Response resp) {
        this.resp = resp;
    }

    @Override
    public InputStream getContent() {
        return this.resp.body().byteStream();
    }

    @Override
    public String getImageFormat() {
        return this.resp.body().contentType().subtype();
    }

    @Override
    public void close() {
        this.resp.body().close();
    }
}
