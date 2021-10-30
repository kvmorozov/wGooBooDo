package ru.kmorozov.onedrive.client

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.http.HttpUnsuccessfulResponseHandler
import com.google.api.client.util.BackOffUtils
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.Sleeper
import ru.kmorozov.onedrive.client.authoriser.AuthorisationProvider
import java.io.IOException

internal class OneDriveResponseHandler(private val authoriser: AuthorisationProvider) : HttpUnsuccessfulResponseHandler {

    private val sleeper = Sleeper.DEFAULT
    private val backOff = ExponentialBackOff()

    @Throws(IOException::class)
    override fun handleResponse(request: HttpRequest, response: HttpResponse, supportsRetry: Boolean): Boolean {

        if (!supportsRetry) {
            return false
        }

        if (HttpStatusCodes.STATUS_CODE_UNAUTHORIZED == response.statusCode) {
            authoriser.refresh()
            return true
        }

        // check if back-off is required for this response
        if (isRequired(response)) {
            try {
                return BackOffUtils.next(sleeper, backOff)
            } catch (exception: InterruptedException) {
                // ignore
            }

        }

        return false
    }

    companion object {

        fun isRequired(httpResponse: HttpResponse): Boolean {
            return 5 == httpResponse.statusCode / 100 || 429 == httpResponse.statusCode
        }
    }

}
