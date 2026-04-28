package com.mysunriser.backend.service;

import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.dto.AuthLoginRequest;
import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.dto.AuthMeResponse;
import com.mysunriser.backend.dto.AuthRegisterRequest;
import com.mysunriser.backend.dto.AuthTokenResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.entity.UserAccount;
import com.mysunriser.backend.exception.BizException;
import com.mysunriser.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppSettingService appSettingService;
    private final AuthRateLimitService authRateLimitService;
    private final TurnstileService turnstileService;
    private final long jwtExpireMinutes;

    public AuthService(
            UserDao userDao,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppSettingService appSettingService,
            AuthRateLimitService authRateLimitService,
            TurnstileService turnstileService,
            @Value("${jwt.expireMinutes}") long jwtExpireMinutes
    ) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appSettingService = appSettingService;
        this.authRateLimitService = authRateLimitService;
        this.turnstileService = turnstileService;
        this.jwtExpireMinutes = jwtExpireMinutes;
    }

    public AuthTokenResponse register(AuthRegisterRequest request, String clientIp) {
        turnstileService.verify(request.turnstileToken(), clientIp);

        if (!appSettingService.isRegistrationEnabled()) {
            throw new BizException(Codes.FORBIDDEN, "registration is disabled");
        }

        String username = request.username().trim();
        UserAccount existingUser = userDao.findByUsername(username);
        if (existingUser != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("user");
        user.setStatus("active");
        user.setTokenVersion(0);

        userDao.insert(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole(), tokenVersion(user));
        return new AuthTokenResponse(token, jwtExpireMinutes, user.getUsername(), user.getRole());
    }

    public AuthTokenResponse login(AuthLoginRequest request, String clientIp) {
        turnstileService.verify(request.turnstileToken(), clientIp);

        String username = request.username().trim();
        authRateLimitService.assertAllowed(username, clientIp);

        UserAccount user = userDao.findByUsername(username);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            authRateLimitService.recordFailure(username, clientIp);
            throw new BizException(Codes.UNAUTHORIZED, "invalid username or password");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.FORBIDDEN, "account disabled");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userDao.updateById(user);
        authRateLimitService.recordSuccess(username, clientIp);

        String token = jwtService.generateToken(user.getUsername(), user.getRole(), tokenVersion(user));
        return new AuthTokenResponse(token, jwtExpireMinutes, user.getUsername(), normalizeRole(user.getRole()));
    }

    public AuthMeResponse me(String username) {
        UserAccount user = userDao.findByUsername(username);
        if (user == null) {
            throw new BizException(Codes.UNAUTHORIZED, "invalid token");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.FORBIDDEN, "account disabled");
        }

        return new AuthMeResponse(user.getUsername(), normalizeRole(user.getRole()), user.getStatus());
    }

    public AuthConfigResponse config() {
        return appSettingService.getPublicAuthConfig();
    }

    private String normalizeRole(String role) {
        return role == null ? "user" : role.toLowerCase(Locale.ROOT);
    }

    private int tokenVersion(UserAccount user) {
        return user.getTokenVersion() == null ? 0 : user.getTokenVersion();
    }
}
