package com.nibado.example.googleoauth.oauth;

import java.time.Instant;

public record Token(
    String accessToken,
    Instant expires,
    String refreshToken) {}
