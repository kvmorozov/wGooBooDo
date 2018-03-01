package com.kmorozov.onedrive.client;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.kmorozov.onedrive.client.authoriser.AuthorisationProvider;

import java.io.IOException;

class OneDriveResponseHandler implements HttpUnsuccessfulResponseHandler {

    private final Sleeper sleeper = Sleeper.DEFAULT;
    private final BackOff backOff = new ExponentialBackOff();
    private final AuthorisationProvider authoriser;

    OneDriveResponseHandler(final AuthorisationProvider authoriser) {
        this.authoriser = authoriser;
    }

    @Override
    public boolean handleResponse(final HttpRequest request, final HttpResponse response, final boolean supportsRetry) throws IOException {

        if (!supportsRetry) {
            return false;
        }

        if (HttpStatusCodes.STATUS_CODE_UNAUTHORIZED == response.getStatusCode()) {
            authoriser.refresh();
            return true;
        }

        // check if back-off is required for this response
        if (isRequired(response)) {
            try {
                return BackOffUtils.next(sleeper, backOff);
            } catch (final InterruptedException exception) {
                // ignore
            }
        }

        return false;
    }

    public static boolean isRequired(final HttpResponse httpResponse) {
        return 5 == httpResponse.getStatusCode() / 100 || 429 == httpResponse.getStatusCode();
    }

}
