package ru.simpleGBD.App.Logic.connectors.apache;

import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.entity.EntityUtils;
import ru.simpleGBD.App.Logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 20.05.2016.
 */
public class ApacheResponse implements Response {

    private final HttpResponse response;

    public ApacheResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public InputStream getContent() throws IOException {
        return response.getEntity().getContent();
    }

    @Override
    public String getImageFormat() {
        String contentType = response.getEntity().getContentType();

        return contentType.startsWith("image/")? contentType.split("/")[1] : null;
    }

    @Override
    public void close() throws IOException {
        EntityUtils.consumeQuietly(response.getEntity());
    }
}
