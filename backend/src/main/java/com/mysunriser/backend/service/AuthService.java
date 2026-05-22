package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mysunriser.backend.Dao.AuthRefreshTokenDao;
import com.mysunriser.backend.Dao.PasswordResetTokenDao;
import com.mysunriser.backend.Dao.PendingRegistrationDao;
import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.dto.AuthConfigResponse;
import com.mysunriser.backend.dto.AuthLoginRequest;
import com.mysunriser.backend.dto.AuthMeResponse;
import com.mysunriser.backend.dto.AuthRegisterRequest;
import com.mysunriser.backend.dto.AuthTokenResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.PasswordForgotRequest;
import com.mysunriser.backend.dto.PasswordResetRequest;
import com.mysunriser.backend.entity.AuthRefreshToken;
import com.mysunriser.backend.entity.PasswordResetToken;
import com.mysunriser.backend.entity.PendingRegistration;
import com.mysunriser.backend.entity.UserAccount;
import com.mysunriser.backend.exception.BizException;
import com.mysunriser.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private final UserDao userDao;
    private final AuthRefreshTokenDao authRefreshTokenDao;
    private final PendingRegistrationDao pendingRegistrationDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppSettingService appSettingService;
    private final AuthRateLimitService authRateLimitService;
    private final TurnstileService turnstileService;
    private final SecureTokenService secureTokenService;
    private final EmailNotificationService emailNotificationService;
    private final long jwtExpireMinutes;
    private final long refreshTokenDays;

    public AuthService(
            UserDao userDao,
            AuthRefreshTokenDao authRefreshTokenDao,
            PendingRegistrationDao pendingRegistrationDao,
            PasswordResetTokenDao passwordResetTokenDao,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppSettingService appSettingService,
            AuthRateLimitService authRateLimitService,
            TurnstileService turnstileService,
            SecureTokenService secureTokenService,
            EmailNotificationService emailNotificationService,
            @Value("${jwt.expireMinutes}") long jwtExpireMinutes,
            @Value("${auth.refresh-token-days:30}") long refreshTokenDays
    ) {
        this.userDao = userDao;
        this.authRefreshTokenDao = authRefreshTokenDao;
        this.pendingRegistrationDao = pendingRegistrationDao;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appSettingService = appSettingService;
        this.authRateLimitService = authRateLimitService;
        this.turnstileService = turnstileService;
        this.secureTokenService = secureTokenService;
        this.emailNotificationService = emailNotificationService;
        this.jwtExpireMinutes = jwtExpireMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public void requestRegistration(AuthRegisterRequest request, String clientIp) {
        turnstileService.verify(request.turnstileToken(), clientIp);

        if (!appSettingService.isRegistrationEnabled()) {
            throw new BizException(Codes.FORBIDDEN, "registration is disabled");
        }

        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        if (userDao.findByUsername(username) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "username already exists");
        }

        if (userDao.findByEmail(email) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "email already exists");
        }

        String token = secureTokenService.generateToken();
        PendingRegistration pendingRegistration = new PendingRegistration();
        pendingRegistration.setUsername(username);
        pendingRegistration.setEmail(email);
        pendingRegistration.setPasswordHash(passwordEncoder.encode(request.password()));
        pendingRegistration.setTokenHash(secureTokenService.hash(token));
        pendingRegistration.setExpiresAt(LocalDateTime.now().plusHours(24));

        pendingRegistrationDao.deleteByUsernameOrEmail(username, email);
        pendingRegistrationDao.insert(pendingRegistration);
        emailNotificationService.sendRegistrationConfirmation(email, token);
    }

    public AuthSession confirmRegistration(String token, String clientIp, String userAgent) {
        PendingRegistration pendingRegistration = pendingRegistrationDao.selectOne(
                new LambdaQueryWrapper<PendingRegistration>()
                        .eq(PendingRegistration::getTokenHash, secureTokenService.hash(token))
                        .last("LIMIT 1")
        );

        if (pendingRegistration == null
                || pendingRegistration.getConsumedAt() != null
                || pendingRegistration.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid or expired registration token");
        }

        if (userDao.findByUsername(pendingRegistration.getUsername()) != null
                || userDao.findByEmail(pendingRegistration.getEmail()) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "account already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(pendingRegistration.getUsername());
        user.setEmail(pendingRegistration.getEmail());
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setNickname(pendingRegistration.getUsername());
        user.setPasswordHash(pendingRegistration.getPasswordHash());
        user.setRole("user");
        user.setStatus("active");
        user.setTokenVersion(0);

        userDao.insert(user);
        pendingRegistration.setConsumedAt(LocalDateTime.now());
        pendingRegistrationDao.updateById(pendingRegistration);

        return buildSession(userDao.selectById(user.getId()), clientIp, userAgent);
    }

    public AuthSession login(AuthLoginRequest request, String clientIp, String userAgent) {
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

        return buildSession(user, clientIp, userAgent);
    }

    public AuthSession refresh(String refreshToken, String clientIp, String userAgent) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BizException(Codes.UNAUTHORIZED, "refresh token required");
        }

        AuthRefreshToken currentToken = findRefreshToken(refreshToken);
        if (currentToken == null
                || currentToken.getRevokedAt() != null
                || currentToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(Codes.UNAUTHORIZED, "invalid refresh token");
        }

        UserAccount user = userDao.selectById(currentToken.getUserId());
        if (user == null || !"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.UNAUTHORIZED, "invalid refresh token");
        }

        currentToken.setRevokedAt(LocalDateTime.now());
        authRefreshTokenDao.updateById(currentToken);
        return buildSession(user, clientIp, userAgent);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        AuthRefreshToken currentToken = findRefreshToken(refreshToken);
        if (currentToken != null && currentToken.getRevokedAt() == null) {
            currentToken.setRevokedAt(LocalDateTime.now());
            authRefreshTokenDao.updateById(currentToken);
        }
    }

    public void requestPasswordReset(PasswordForgotRequest request, String clientIp) {
        String email = normalizeEmail(request.email());
        UserAccount user = userDao.findByEmail(email);
        if (user == null || !"active".equalsIgnoreCase(user.getStatus())) {
            return;
        }

        String token = secureTokenService.generateToken();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUserId(user.getId());
        passwordResetToken.setTokenHash(secureTokenService.hash(token));
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        passwordResetToken.setCreatedIp(clientIp);
        passwordResetTokenDao.insert(passwordResetToken);

        emailNotificationService.sendPasswordReset(email, token);
    }

    public void resetPassword(PasswordResetRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenDao.selectOne(
                new LambdaQueryWrapper<PasswordResetToken>()
                        .eq(PasswordResetToken::getTokenHash, secureTokenService.hash(request.token()))
                        .last("LIMIT 1")
        );

        if (passwordResetToken == null
                || passwordResetToken.getConsumedAt() != null
                || passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid or expired reset token");
        }

        UserAccount user = userDao.selectById(passwordResetToken.getUserId());
        if (user == null) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid or expired reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(nextTokenVersion(user));
        userDao.updateById(user);
        revokeRefreshTokensForUser(user.getId());

        passwordResetToken.setConsumedAt(LocalDateTime.now());
        passwordResetTokenDao.updateById(passwordResetToken);
    }

    public void revokeRefreshTokensForUser(Long userId) {
        if (userId == null) {
            return;
        }

        List<AuthRefreshToken> tokens = authRefreshTokenDao.selectList(
                new LambdaQueryWrapper<AuthRefreshToken>()
                        .eq(AuthRefreshToken::getUserId, userId)
                        .isNull(AuthRefreshToken::getRevokedAt)
        );
        LocalDateTime now = LocalDateTime.now();
        for (AuthRefreshToken token : tokens) {
            token.setRevokedAt(now);
            authRefreshTokenDao.updateById(token);
        }
    }

    public AuthMeResponse me(String username) {
        UserAccount user = userDao.findByUsername(username);
        if (user == null) {
            throw new BizException(Codes.UNAUTHORIZED, "invalid token");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.FORBIDDEN, "account disabled");
        }

        return toMeResponse(user);
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

    private int nextTokenVersion(UserAccount user) {
        return tokenVersion(user) + 1;
    }

    private AuthSession buildSession(UserAccount user, String clientIp, String userAgent) {
        String refreshToken = secureTokenService.generateToken();
        AuthRefreshToken authRefreshToken = new AuthRefreshToken();
        authRefreshToken.setUserId(user.getId());
        authRefreshToken.setTokenHash(secureTokenService.hash(refreshToken));
        authRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenDays));
        authRefreshToken.setCreatedIp(truncate(clientIp, 64));
        authRefreshToken.setCreatedUserAgent(truncate(userAgent, 255));
        authRefreshTokenDao.insert(authRefreshToken);

        return new AuthSession(toTokenResponse(user), refreshToken);
    }

    private AuthRefreshToken findRefreshToken(String refreshToken) {
        return authRefreshTokenDao.selectOne(
                new LambdaQueryWrapper<AuthRefreshToken>()
                        .eq(AuthRefreshToken::getTokenHash, secureTokenService.hash(refreshToken))
                        .last("LIMIT 1")
        );
    }

    private AuthTokenResponse toTokenResponse(UserAccount user) {
        String accessToken = jwtService.generateToken(user.getUsername(), normalizeRole(user.getRole()), tokenVersion(user));
        return new AuthTokenResponse(
                accessToken,
                jwtExpireMinutes,
                user.getId(),
                user.getUsername(),
                normalizeRole(user.getRole()),
                safe(user.getEmail()),
                user.getEmailVerifiedAt() != null,
                safe(user.getNickname()),
                safe(user.getAvatarUrl())
        );
    }

    private AuthMeResponse toMeResponse(UserAccount user) {
        return new AuthMeResponse(
                user.getId(),
                user.getUsername(),
                normalizeRole(user.getRole()),
                user.getStatus(),
                safe(user.getEmail()),
                user.getEmailVerifiedAt() != null,
                safe(user.getNickname()),
                safe(user.getAvatarUrl())
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
