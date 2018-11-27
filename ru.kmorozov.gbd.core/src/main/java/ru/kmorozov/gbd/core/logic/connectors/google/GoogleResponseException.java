package ru.kmorozov.gbd.core.logic.connectors.google;

import com.google.api.client.http.HttpResponseException;
import ru.kmorozov.gbd.core.logic.connectors.ResponseException;

/**
 * Created by km on 17.05.2016.
 */
class GoogleResponseException extends ResponseException {

    private final HttpResponseException hre;

    public GoogleResponseException(HttpResponseException hre) {
        super(hre);
        this.hre = hre;
    }

    @Override
    public int getStatusCode() {
        return this.hre.getStatusCode();
    }
}
