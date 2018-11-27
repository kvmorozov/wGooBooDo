package ru.kmorozov.onedrive.client;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.BackOffUtils;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider;

import java.io.IOException;

class OneDriveResponseHandler implements HttpUnsuccessfulResponseHandler {

    private final Sleeper sleeper = Sleeper.DEFAULT;
    private final BackOff backOff = new ExponentialBackOff();
    private final AuthorisationProvider authoriser;

    OneDriveResponseHandler(AuthorisationProvider authoriser) {
        this.authoriser = authoriser;
    }

    @Override
    public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) throws IOException {

        if (!supportsRetry) {
            return false;
        }

        if (HttpStatusCodes.STATUS_CODE_UNAUTHORIZED == response.getStatusCode()) {
            this.authoriser.refresh();
            return true;
        }

        // check if back-off is required for this response
        if (OneDriveResponseHandler.isRequired(response)) {
            try {
                return BackOffUtils.next(this.sleeper, this.backOff);
            } catch (InterruptedException exception) {
                // ignore
            }
        }

        return false;
    }

    public static boolean isRequired(HttpResponse httpResponse) {
        return 5 == httpResponse.getStatusCode() / 100 || 429 == httpResponse.getStatusCode();
    }

}
