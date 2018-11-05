package ru.kmorozov.onedrive.client.authoriser;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kmorozov.onedrive.client.OneDriveAPIException;
import ru.kmorozov.onedrive.client.exceptions.InvalidCodeException;
import ru.kmorozov.onedrive.client.exceptions.OneDriveExceptionFactory;
import ru.kmorozov.onedrive.client.resources.Authorisation;
import ru.kmorozov.onedrive.client.utils.JsonUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class OneDriveAuthorisationProvider implements AuthorisationProvider {

    private static final String[] EMPTY_STR_ARR = new String[0];

    static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
    private static final Logger log = LogManager.getLogger(OneDriveAuthorisationProvider.class.getName());

    private final String clientId;
    private final String clientSecret;

    private static final String scope = "files.readwrite.all";
    private static final String AUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    public static final String REDIRECT_URL = "https://ya.ru/";

    private Path keyFile;
    private Authorisation authorisation;
    private Date lastFetched;

    OneDriveAuthorisationProvider(final Path keyFile, String clientId, String clientSecret) throws IOException {
        this.keyFile = Preconditions.checkNotNull(keyFile);
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        if (!Files.exists(keyFile) || !Files.isRegularFile(keyFile)) {
            throw new OneDriveAPIException(401, String.format("Specified key file '%s' cannot be found.", keyFile));
        }

        final String[] keyFileContents = readToken();

        switch (keyFileContents.length) {
            case 0:
                throw new OneDriveAPIException(401, String.format("Key file '%s' is empty.", keyFile));
            case 1:
                String authCode = keyFileContents[0];

                if (Strings.isNullOrEmpty(authCode))
                    throw new InvalidCodeException(null);

                // If the user has pasted the entire URL then parse it
                final Pattern url = Pattern.compile(REDIRECT_URL + ".*code=(.*)");
                final Matcher m = url.matcher(authCode);

                if (m.matches()) {
                    authCode = m.group(1);
                }

                getTokenFromCode(authCode);
                break;
            case 2:
                if (keyFileContents[0].equals(clientId)) {
                    getTokenFromRefreshToken(keyFileContents[1]);
                } else {
                    throw new OneDriveAPIException(401, "Key file does not match this application version.");
                }
                break;
            default:
                throw new OneDriveAPIException(401, "Expected key file with code and/or refresh token");
        }
    }

    public static void printAuthInstructions(String clientId) {
        log.info("To authorise this application ou must generate an authorisation token");
        log.info("Open the following in a browser, sign on, wait until you are redirected to a blank page and then store the url in the address bar in your key file.");
        log.info("Authorisation URL: " + getAuthString(clientId));
    }

    public static String getAuthString(String clientId) {
        return String.format("%s?client_id=%s&response_type=code&scope=%s&redirect_uri=%s",
                AUTH_URL,
                clientId,
                scope,
                REDIRECT_URL);
    }

    @Override
    public String getAccessToken() throws IOException {
        if (null != authorisation) {

            // Refresh if we know it is needed
            if (lastFetched.after(new Date(lastFetched.getTime() + authorisation.getExpiresIn() * 1000))) {
                log.info("Authorisation token has expired - refreshing");
                getTokenFromRefreshToken(authorisation.getRefreshToken());
                saveToken();
            }

            return authorisation.getAccessToken();
        } else {
            throw new IllegalStateException("Authoriser has not been initialised");
        }
    }

    public void refresh() throws IOException {
        getTokenFromRefreshToken(authorisation.getRefreshToken());
        saveToken();
    }

    private void getTokenFromCode(final String code) throws IOException {
        log.debug("Fetching authorisation token using authorisation code");

        final Map data = Collections.unmodifiableMap(Stream.of(
                new SimpleEntry<>("client_id", clientId),
                new SimpleEntry<>("code", code),
                new SimpleEntry<>("client_secret", clientSecret),
                new SimpleEntry<>("grant_type", "authorization_code"),
                new SimpleEntry<>("redirect_uri", REDIRECT_URL))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

        final HttpRequest request =
                HTTP_TRANSPORT.createRequestFactory().buildPostRequest(new GenericUrl(TOKEN_URL), new UrlEncodedContent(data));

        request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));

        processResponse(getResponse(request));
    }

    private static HttpResponse getResponse(final HttpRequest request) throws IOException {
        final HttpResponse response;

        try {
            response = request.execute();
        } catch (final HttpResponseException hre) {
            throw OneDriveExceptionFactory.getException(hre.getContent());
        } catch (final SSLHandshakeException she) {
            log.error("Failed to validate certificates!");
//            if (she.getCause() instanceof ValidatorException)
//                if (she.getCause().getCause() instanceof SunCertPathBuilderException) {
//                    final SunCertPathBuilderException certEx = (SunCertPathBuilderException) she.getCause().getCause();
//                    final Iterator<BuildStep> steps = certEx.getAdjacencyList().iterator();
//                    while (steps.hasNext()) {
//                        final BuildStep step = steps.next();
//                        log.info(step.getVertex().toString());
//                    }
//                }
            throw OneDriveExceptionFactory.getException(she.getMessage());
        }

        return response;
    }

    private void getTokenFromRefreshToken(final String refreshToken) throws IOException {
        log.debug("Fetching authorisation token using refresh token");

        if (StringUtils.isEmpty(refreshToken))
            throw new InvalidCodeException(null);

        final Map data = Collections.unmodifiableMap(Stream.of(
                new SimpleEntry<>("client_id", clientId),
                new SimpleEntry<>("client_secret", clientSecret),
                new SimpleEntry<>("grant_type", "refresh_token"),
                new SimpleEntry<>("refresh_token", refreshToken),
                new SimpleEntry<>("redirect_uri", REDIRECT_URL))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

        final HttpRequest request =
                HTTP_TRANSPORT.createRequestFactory().buildPostRequest(new GenericUrl(TOKEN_URL), new UrlEncodedContent(data));

        request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));

        processResponse(getResponse(request));
    }

    private void processResponse(final HttpResponse response) throws IOException {
        authorisation = response.parseAs(Authorisation.class);

        // Check for failures
        if (200 != response.getStatusCode() || null != authorisation.getError()) {
            throw new OneDriveAPIException(response.getStatusCode(),
                    String.format("Error code %d - %s (%s)",
                            response.getStatusCode(),
                            authorisation.getError(),
                            authorisation.getErrorDescription()));
        }

        log.info("Fetched new authorisation token and refresh token for user " + authorisation.getUserId());
        saveToken();
        lastFetched = new Date();
    }

    private String[] readToken() {
        try {
            return Files.readAllLines(keyFile, Charset.defaultCharset()).toArray(new String[1]);
        } catch (final IOException e) {
            log.error("Unable to read key file", e);
        }

        return EMPTY_STR_ARR;
    }

    private void saveToken() {
        try {
            final String[] content = {clientId, authorisation.getRefreshToken()};
            Files.write(keyFile, Arrays.asList(content), Charset.defaultCharset());
        } catch (final IOException e) {
            log.error("Unable to write to key file ", e);
        }
    }
}
