package ru.simpleGBD.App.Logic.connectors.apache;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import ru.simpleGBD.App.Logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 20.05.2016.
 */
public class ApacheResponse implements Response {

    private HttpResponse response;

    public ApacheResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public InputStream getContent() throws IOException {
        return response.getEntity().getContent();
    }

    @Override
    public String getImageFormat() {
        String contentType = response.getEntity().getContentType().toString();

        return contentType.startsWith("image/")? contentType.split("/")[1] : null;
    }

    @Override
    public void close() throws IOException {
        EntityUtils.consumeQuietly(response.getEntity());
    }
}
