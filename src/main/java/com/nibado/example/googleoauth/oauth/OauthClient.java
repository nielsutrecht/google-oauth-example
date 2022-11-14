package com.nibado.example.googleoauth.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

public class OauthClient {
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final OkHttpClient client;
    private final Properties properties;
    private final ObjectMapper mapper;

    public OauthClient(Properties properties) {
        this.client = new OkHttpClient();
        this.properties = properties;
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }

    public String buildRedirect() {
        var builder = new StringBuilder();

        builder.append("https://accounts.google.com/o/oauth2/v2/auth")
            .append("?scope=https://www.googleapis.com/auth/calendar.events.readonly%20https://www.googleapis.com/auth/calendar.readonly")
            .append("&access_type=offline")
            .append("&include_granted_scopes=true")
            .append("&redirect_uri=http://localhost:8000/oauth")
            .append("&response_type=code")
            .append("&client_id=")
            .append(properties.getProperty("client-id"));

        return builder.toString();
    }

    public Token exchange(String code) throws IOException {
        var body = new FormBody.Builder()
            .add("client_id", properties.getProperty("client-id"))
            .add("client_secret", properties.getProperty("client-secret"))
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", "http://localhost:8000/oauth")
            .build();

        return doTokenRequest(body);
    }

    public Token refresh(Token token) throws IOException {
        var body = new FormBody.Builder()
            .add("client_id", properties.getProperty("client-id"))
            .add("client_secret", properties.getProperty("client-secret"))
            .add("refresh_token", token.refreshToken())
            .add("grant_type", "refresh_token")
            .build();

        return doTokenRequest(body);
    }

    private Token doTokenRequest(FormBody body) throws IOException {
        var request = new Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .build();

        try (var response = client.newCall(request).execute()) {
            var token = mapper.readValue(response.body().bytes(), TokenResponse.class);

            return new Token(token.accessToken, Instant.now().plusSeconds(token.expiresIn), token.refreshToken);
        }
    }

    private record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,  //Expiration in seconds
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("scope") String scope,
        @JsonProperty("token_type") String tokenType) {}
}
