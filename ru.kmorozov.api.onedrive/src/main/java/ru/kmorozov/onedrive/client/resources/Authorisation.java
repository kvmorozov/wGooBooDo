package ru.kmorozov.onedrive.client.resources;

import com.google.api.client.util.Key;

public class Authorisation {

    @Key("token_type")
    private String tokenType;
    @Key("expires_in")
    private int expiresIn;
    @Key("scope")
    private String scope;
    @Key("access_token")
    private String accessToken;
    @Key("refresh_token")
    private String refreshToken;
    @Key("user_id")
    private String userId;
    @Key("error")
    private String error;
    @Key("error_description")
    private String errorDescription;

    public String getTokenType() {
        return this.tokenType;
    }

    public int getExpiresIn() {
        return this.expiresIn;
    }

    public String getScope() {
        return this.scope;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getError() {
        return this.error;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }
}
