package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mysunriser.backend.Dao.AccountEmailChangeTokenDao;
import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.dto.AccountEmailChangeRequest;
import com.mysunriser.backend.dto.AccountPasswordUpdateRequest;
import com.mysunriser.backend.dto.AccountProfileResponse;
import com.mysunriser.backend.dto.AccountProfileUpdateRequest;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.entity.AccountEmailChangeToken;
import com.mysunriser.backend.entity.UserAccount;
import com.mysunriser.backend.exception.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AccountService {

    private final UserDao userDao;
    private final AccountEmailChangeTokenDao emailChangeTokenDao;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenService secureTokenService;
    private final EmailNotificationService emailNotificationService;
    private final AuthService authService;

    public AccountService(
            UserDao userDao,
            AccountEmailChangeTokenDao emailChangeTokenDao,
            PasswordEncoder passwordEncoder,
            SecureTokenService secureTokenService,
            EmailNotificationService emailNotificationService,
            AuthService authService
    ) {
        this.userDao = userDao;
        this.emailChangeTokenDao = emailChangeTokenDao;
        this.passwordEncoder = passwordEncoder;
        this.secureTokenService = secureTokenService;
        this.emailNotificationService = emailNotificationService;
        this.authService = authService;
    }

    public AccountProfileResponse profile(String username) {
        return toResponse(loadActiveUser(username));
    }

    public AccountProfileResponse updateProfile(String username, AccountProfileUpdateRequest request) {
        UserAccount user = loadActiveUser(username);
        user.setNickname(normalizeNullable(request.nickname(), 64));
        userDao.updateById(user);
        return toResponse(userDao.selectById(user.getId()));
    }

    public void changePassword(String username, AccountPasswordUpdateRequest request) {
        UserAccount user = loadActiveUser(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BizException(Codes.UNAUTHORIZED, "current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        userDao.updateById(user);
        authService.revokeRefreshTokensForUser(user.getId());
    }

    public void requestEmailChange(String username, AccountEmailChangeRequest request) {
        UserAccount user = loadActiveUser(username);
        String email = normalizeEmail(request.email());
        UserAccount existingUser = userDao.findByEmail(email);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new BizException(Codes.VALIDATION_ERROR, "email already exists");
        }

        String token = secureTokenService.generateToken();
        AccountEmailChangeToken emailChangeToken = new AccountEmailChangeToken();
        emailChangeToken.setUserId(user.getId());
        emailChangeToken.setEmail(email);
        emailChangeToken.setTokenHash(secureTokenService.hash(token));
        emailChangeToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        emailChangeTokenDao.deleteByUserId(user.getId());
        emailChangeTokenDao.insert(emailChangeToken);
        emailNotificationService.sendEmailChangeConfirmation(email, token);
    }

    public void confirmEmailChange(String token) {
        AccountEmailChangeToken emailChangeToken = emailChangeTokenDao.selectOne(
                new LambdaQueryWrapper<AccountEmailChangeToken>()
                        .eq(AccountEmailChangeToken::getTokenHash, secureTokenService.hash(token))
                        .last("LIMIT 1")
        );

        if (emailChangeToken == null
                || emailChangeToken.getConsumedAt() != null
                || emailChangeToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid or expired email change token");
        }

        UserAccount user = userDao.selectById(emailChangeToken.getUserId());
        if (user == null || !"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid or expired email change token");
        }

        UserAccount existingUser = userDao.findByEmail(emailChangeToken.getEmail());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new BizException(Codes.VALIDATION_ERROR, "email already exists");
        }

        user.setEmail(emailChangeToken.getEmail());
        user.setEmailVerifiedAt(LocalDateTime.now());
        userDao.updateById(user);

        emailChangeToken.setConsumedAt(LocalDateTime.now());
        emailChangeTokenDao.updateById(emailChangeToken);
    }

    private UserAccount loadActiveUser(String username) {
        UserAccount user = userDao.findByUsername(username);
        if (user == null) {
            throw new BizException(Codes.UNAUTHORIZED, "unauthorized");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(Codes.FORBIDDEN, "account disabled");
        }

        return user;
    }

    private AccountProfileResponse toResponse(UserAccount user) {
        return new AccountProfileResponse(
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

    private String normalizeRole(String role) {
        return role == null ? "user" : role.toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
