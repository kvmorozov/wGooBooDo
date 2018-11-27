package ru.kmorozov.gbd.core.logic.connectors.webdriver;

import org.apache.commons.io.input.CharSequenceInputStream;
import ru.kmorozov.gbd.core.logic.connectors.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

class WebDriverResponse implements Response {

    private final String response;

    WebDriverResponse(final String response) {
        this.response = response;
    }


    @Override
    public InputStream getContent() throws IOException {
        return new CharSequenceInputStream(this.response, Charset.forName("UTF-8"));
    }

    @Override
    public String getImageFormat() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
