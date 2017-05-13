package ru.kmorozov.gbd.core.logic.connectors.apache;

import org.apache.hc.client5.http.impl.sync.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by km on 20.05.2016.
 */
public class ApacheResponse implements Response {

    private final CloseableHttpResponse response;

    ApacheResponse(CloseableHttpResponse response) {
        this.response = response;
    }

    @Override
    public InputStream getContent() throws IOException {
        return response == null ? null : response.getEntity().getContent();
    }

    @Override
    public String getImageFormat() {
        String contentType = response.getEntity().getContentType();

        return contentType.startsWith("image/") ? contentType.split("/")[1] : null;
    }

    @Override
    public void close() throws IOException {
        if (response != null) EntityUtils.consumeQuietly(response.getEntity());
    }
}
