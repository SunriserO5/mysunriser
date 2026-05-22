package com.mysunriser.backend.service;

import com.mysunriser.backend.dto.AuthTokenResponse;

public record AuthSession(
        AuthTokenResponse response,
        String refreshToken
) {
}
