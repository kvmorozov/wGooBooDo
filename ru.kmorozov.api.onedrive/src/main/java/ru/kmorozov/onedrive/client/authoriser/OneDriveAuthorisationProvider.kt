package ru.kmorozov.onedrive.client.authoriser

import com.google.api.client.http.*
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.Preconditions
import com.google.api.client.util.Strings
import org.apache.logging.log4j.LogManager
import ru.kmorozov.onedrive.client.OneDriveAPIException
import ru.kmorozov.onedrive.client.exceptions.InvalidCodeException
import ru.kmorozov.onedrive.client.exceptions.OneDriveErrorInfo
import ru.kmorozov.onedrive.client.exceptions.OneDriveExceptionFactory
import ru.kmorozov.onedrive.client.resources.Authorisation
import ru.kmorozov.onedrive.client.utils.JsonUtils
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.SSLHandshakeException

internal class OneDriveAuthorisationProvider @Throws(IOException::class)
constructor(keyFile: Path, private val clientId: String, private val clientSecret: String) : AuthorisationProvider {

    private val keyFile: Path
    private var authorisation: Authorisation? = null
    private var lastFetched: Date = Date(Long.MIN_VALUE)

    override// Refresh if we know it is needed
    val accessToken: String
        @Throws(IOException::class)
        get() {
            if (null != authorisation) {
                if (lastFetched.after(Date(lastFetched.time + (authorisation!!.expiresIn * 1000).toLong()))) {
                    log.info("Authorisation token has expired - refreshing")
                    getTokenFromRefreshToken(authorisation!!.refreshToken!!)
                    saveToken()
                }

                return authorisation!!.accessToken!!
            } else {
                throw IllegalStateException("Authoriser has not been initialised")
            }
        }

    init {
        this.keyFile = Preconditions.checkNotNull(keyFile)

        if (!Files.exists(keyFile) || !Files.isRegularFile(keyFile)) {
            throw OneDriveAPIException(401, "Specified key file '$keyFile' cannot be found.")
        }

        val keyFileContents = readToken()

        when (keyFileContents.size) {
            0 -> throw OneDriveAPIException(401, "Key file '$keyFile' is empty.")
            1 -> {
                var authCode = keyFileContents[0]

                if (Strings.isNullOrEmpty(authCode))
                    throw InvalidCodeException(OneDriveErrorInfo())

                // If the user has pasted the entire URL then parse it
                val url = Pattern.compile("$REDIRECT_URL.*code=(.*)")
                val m = url.matcher(authCode)

                if (m.matches()) {
                    authCode = m.group(1)
                }

                getTokenFromCode(authCode)
            }
            2 -> if (keyFileContents[0] == clientId) {
                getTokenFromRefreshToken(keyFileContents[1])
            } else {
                throw OneDriveAPIException(401, "Key file does not match this application version.")
            }
            else -> throw OneDriveAPIException(401, "Expected key file with code and/or refresh token")
        }
    }

    @Throws(IOException::class)
    override fun refresh() {
        getTokenFromRefreshToken(authorisation!!.refreshToken!!)
        saveToken()
    }

    @Throws(IOException::class)
    private fun getTokenFromCode(code: String) {
        log.debug("Fetching authorisation token using authorisation code")

        val data = mapOf<String, String>(
                "client_id" to clientId,
                "code" to code,
                "client_secret" to clientSecret,
                "grant_type" to "authorization_code",
                "redirect_uri" to REDIRECT_URL)

        val request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(GenericUrl(TOKEN_URL), UrlEncodedContent(data))

        request.parser = JsonObjectParser(JsonUtils.JSON_FACTORY)

        processResponse(getResponse(request))
    }

    @Throws(IOException::class)
    private fun getTokenFromRefreshToken(refreshToken: String) {
        log.debug("Fetching authorisation token using refresh token")

        if (Strings.isNullOrEmpty(refreshToken))
            throw InvalidCodeException(OneDriveErrorInfo())

        val data = mapOf<String, String>(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
                "redirect_uri" to REDIRECT_URL)

        val request = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(GenericUrl(TOKEN_URL), UrlEncodedContent(data))

        request.parser = JsonObjectParser(JsonUtils.JSON_FACTORY)

        processResponse(getResponse(request))
    }

    @Throws(IOException::class)
    private fun processResponse(response: HttpResponse) {
        authorisation = response.parseAs(Authorisation::class.java)

        // Check for failures
        if (200 != response.statusCode || null != authorisation!!.error) {
            throw OneDriveAPIException(response.statusCode,
                    String.format("Error code %d - %s (%s)",
                            response.statusCode,
                            authorisation!!.error,
                            authorisation!!.errorDescription))
        }

        log.info("Fetched new authorisation token and refresh token for user " + authorisation!!.userId!!)
        saveToken()
        lastFetched = Date()
    }

    private fun readToken(): Array<String> {
        try {
            return Files.readAllLines(keyFile, Charset.defaultCharset()).toTypedArray()
        } catch (e: IOException) {
            log.error("Unable to read key file", e)
        }

        return EMPTY_STR_ARR
    }

    private fun saveToken() {
        try {
            val content = arrayOf(clientId, authorisation!!.refreshToken)
            Files.write(keyFile, Arrays.asList(*content), Charset.defaultCharset())
        } catch (e: IOException) {
            log.error("Unable to write to key file ", e)
        }

    }

    companion object {

        private val EMPTY_STR_ARR = arrayOf<String>("")

        val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
        private val log = LogManager.getLogger(OneDriveAuthorisationProvider::class.java.name)

        private const val scope = "files.readwrite.all"
        private const val AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
        private const val TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token"
        const val REDIRECT_URL = "https://ya.ru/"

        fun printAuthInstructions(clientId: String) {
            log.info("To authorise this application ou must generate an authorisation token")
            log.info("Open the following in a browser, sign on, wait until you are redirected to a blank page and then store the url in the address bar in your key file.")
            log.info("Authorisation URL: " + getAuthString(clientId))
        }

        fun getAuthString(clientId: String): String {
            return "$AUTH_URL?client_id=$clientId&response_type=code&scope=$scope&redirect_uri=$REDIRECT_URL"
        }

        @Throws(IOException::class)
        private fun getResponse(request: HttpRequest): HttpResponse {
            val response: HttpResponse

            try {
                response = request.execute()
            } catch (hre: HttpResponseException) {
                throw OneDriveExceptionFactory.getException(hre.content)
            } catch (she: SSLHandshakeException) {
                log.error("Failed to validate certificates!")
                //            if (she.getCause() instanceof ValidatorException)
                //                if (she.getCause().getCause() instanceof SunCertPathBuilderException) {
                //                    final SunCertPathBuilderException certEx = (SunCertPathBuilderException) she.getCause().getCause();
                //                    final Iterator<BuildStep> steps = certEx.getAdjacencyList().iterator();
                //                    while (steps.hasNext()) {
                //                        final BuildStep step = steps.next();
                //                        log.info(step.getVertex().toString());
                //                    }
                //                }
                throw OneDriveExceptionFactory.getException(she.message!!)
            }

            return response
        }
    }
}
