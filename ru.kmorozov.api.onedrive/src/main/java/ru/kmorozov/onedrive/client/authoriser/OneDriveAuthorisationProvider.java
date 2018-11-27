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
import java.util.AbstractMap;
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

    private final Path keyFile;
    private Authorisation authorisation;
    private Date lastFetched;

    OneDriveAuthorisationProvider(Path keyFile, final String clientId, final String clientSecret) throws IOException {
        this.keyFile = Preconditions.checkNotNull(keyFile);
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        if (!Files.exists(keyFile) || !Files.isRegularFile(keyFile)) {
            throw new OneDriveAPIException(401, String.format("Specified key file '%s' cannot be found.", keyFile));
        }

        String[] keyFileContents = this.readToken();

        switch (keyFileContents.length) {
            case 0:
                throw new OneDriveAPIException(401, String.format("Key file '%s' is empty.", keyFile));
            case 1:
                String authCode = keyFileContents[0];

                if (Strings.isNullOrEmpty(authCode))
                    throw new InvalidCodeException(null);

                // If the user has pasted the entire URL then parse it
                Pattern url = Pattern.compile(OneDriveAuthorisationProvider.REDIRECT_URL + ".*code=(.*)");
                Matcher m = url.matcher(authCode);

                if (m.matches()) {
                    authCode = m.group(1);
                }

                this.getTokenFromCode(authCode);
                break;
            case 2:
                if (keyFileContents[0].equals(clientId)) {
                    this.getTokenFromRefreshToken(keyFileContents[1]);
                } else {
                    throw new OneDriveAPIException(401, "Key file does not match this application version.");
                }
                break;
            default:
                throw new OneDriveAPIException(401, "Expected key file with code and/or refresh token");
        }
    }

    public static void printAuthInstructions(final String clientId) {
        OneDriveAuthorisationProvider.log.info("To authorise this application ou must generate an authorisation token");
        OneDriveAuthorisationProvider.log.info("Open the following in a browser, sign on, wait until you are redirected to a blank page and then store the url in the address bar in your key file.");
        OneDriveAuthorisationProvider.log.info("Authorisation URL: " + OneDriveAuthorisationProvider.getAuthString(clientId));
    }

    public static String getAuthString(final String clientId) {
        return String.format("%s?client_id=%s&response_type=code&scope=%s&redirect_uri=%s",
                OneDriveAuthorisationProvider.AUTH_URL,
                clientId,
                OneDriveAuthorisationProvider.scope,
                OneDriveAuthorisationProvider.REDIRECT_URL);
    }

    @Override
    public String getAccessToken() throws IOException {
        if (null != this.authorisation) {

            // Refresh if we know it is needed
            if (this.lastFetched.after(new Date(this.lastFetched.getTime() + (long) (this.authorisation.getExpiresIn() * 1000)))) {
                OneDriveAuthorisationProvider.log.info("Authorisation token has expired - refreshing");
                this.getTokenFromRefreshToken(this.authorisation.getRefreshToken());
                this.saveToken();
            }

            return this.authorisation.getAccessToken();
        } else {
            throw new IllegalStateException("Authoriser has not been initialised");
        }
    }

    public void refresh() throws IOException {
        this.getTokenFromRefreshToken(this.authorisation.getRefreshToken());
        this.saveToken();
    }

    private void getTokenFromCode(String code) throws IOException {
        OneDriveAuthorisationProvider.log.debug("Fetching authorisation token using authorisation code");

        Map data = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("client_id", this.clientId),
                new AbstractMap.SimpleEntry<>("code", code),
                new AbstractMap.SimpleEntry<>("client_secret", this.clientSecret),
                new AbstractMap.SimpleEntry<>("grant_type", "authorization_code"),
                new AbstractMap.SimpleEntry<>("redirect_uri", OneDriveAuthorisationProvider.REDIRECT_URL))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

        HttpRequest request =
                OneDriveAuthorisationProvider.HTTP_TRANSPORT.createRequestFactory().buildPostRequest(new GenericUrl(OneDriveAuthorisationProvider.TOKEN_URL), new UrlEncodedContent(data));

        request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));

        this.processResponse(OneDriveAuthorisationProvider.getResponse(request));
    }

    private static HttpResponse getResponse(HttpRequest request) throws IOException {
        HttpResponse response;

        try {
            response = request.execute();
        } catch (HttpResponseException hre) {
            throw OneDriveExceptionFactory.getException(hre.getContent());
        } catch (SSLHandshakeException she) {
            OneDriveAuthorisationProvider.log.error("Failed to validate certificates!");
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

    private void getTokenFromRefreshToken(String refreshToken) throws IOException {
        OneDriveAuthorisationProvider.log.debug("Fetching authorisation token using refresh token");

        if (StringUtils.isEmpty(refreshToken))
            throw new InvalidCodeException(null);

        Map data = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("client_id", this.clientId),
                new AbstractMap.SimpleEntry<>("client_secret", this.clientSecret),
                new AbstractMap.SimpleEntry<>("grant_type", "refresh_token"),
                new AbstractMap.SimpleEntry<>("refresh_token", refreshToken),
                new AbstractMap.SimpleEntry<>("redirect_uri", OneDriveAuthorisationProvider.REDIRECT_URL))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

        HttpRequest request =
                OneDriveAuthorisationProvider.HTTP_TRANSPORT.createRequestFactory().buildPostRequest(new GenericUrl(OneDriveAuthorisationProvider.TOKEN_URL), new UrlEncodedContent(data));

        request.setParser(new JsonObjectParser(JsonUtils.JSON_FACTORY));

        this.processResponse(OneDriveAuthorisationProvider.getResponse(request));
    }

    private void processResponse(HttpResponse response) throws IOException {
        this.authorisation = response.parseAs(Authorisation.class);

        // Check for failures
        if (200 != response.getStatusCode() || null != this.authorisation.getError()) {
            throw new OneDriveAPIException(response.getStatusCode(),
                    String.format("Error code %d - %s (%s)",
                            response.getStatusCode(),
                            this.authorisation.getError(),
                            this.authorisation.getErrorDescription()));
        }

        OneDriveAuthorisationProvider.log.info("Fetched new authorisation token and refresh token for user " + this.authorisation.getUserId());
        this.saveToken();
        this.lastFetched = new Date();
    }

    private String[] readToken() {
        try {
            return Files.readAllLines(this.keyFile, Charset.defaultCharset()).toArray(new String[1]);
        } catch (IOException e) {
            OneDriveAuthorisationProvider.log.error("Unable to read key file", e);
        }

        return OneDriveAuthorisationProvider.EMPTY_STR_ARR;
    }

    private void saveToken() {
        try {
            String[] content = {this.clientId, this.authorisation.getRefreshToken()};
            Files.write(this.keyFile, Arrays.asList(content), Charset.defaultCharset());
        } catch (IOException e) {
            OneDriveAuthorisationProvider.log.error("Unable to write to key file ", e);
        }
    }
}
