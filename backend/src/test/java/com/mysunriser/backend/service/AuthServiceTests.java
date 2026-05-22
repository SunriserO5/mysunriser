package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.mysunriser.backend.Dao.AuthRefreshTokenDao;
import com.mysunriser.backend.Dao.PasswordResetTokenDao;
import com.mysunriser.backend.Dao.PendingRegistrationDao;
import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.dto.AuthLoginRequest;
import com.mysunriser.backend.dto.AuthRegisterRequest;
import com.mysunriser.backend.dto.PasswordForgotRequest;
import com.mysunriser.backend.entity.AuthRefreshToken;
import com.mysunriser.backend.entity.PasswordResetToken;
import com.mysunriser.backend.entity.PendingRegistration;
import com.mysunriser.backend.entity.UserAccount;
import com.mysunriser.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserDao userDao;

    @Mock
    private AuthRefreshTokenDao authRefreshTokenDao;

    @Mock
    private PendingRegistrationDao pendingRegistrationDao;

    @Mock
    private PasswordResetTokenDao passwordResetTokenDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private AuthRateLimitService authRateLimitService;

    @Mock
    private TurnstileService turnstileService;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private EmailNotificationService emailNotificationService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userDao,
                authRefreshTokenDao,
                pendingRegistrationDao,
                passwordResetTokenDao,
                passwordEncoder,
                jwtService,
                appSettingService,
                authRateLimitService,
                turnstileService,
                secureTokenService,
                emailNotificationService,
                15,
                30
        );
    }

    @Test
    void requestRegistrationCreatesPendingRecordAndSendsEmail() {
        when(appSettingService.isRegistrationEnabled()).thenReturn(true);
        when(userDao.findByUsername("sun")).thenReturn(null);
        when(userDao.findByEmail("sun@example.com")).thenReturn(null);
        when(secureTokenService.generateToken()).thenReturn("registration-token");
        when(secureTokenService.hash("registration-token")).thenReturn("registration-hash");

        authService.requestRegistration(
                new AuthRegisterRequest("sun", "SUN@example.com", "password123", null),
                "127.0.0.1"
        );

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationDao).deleteByUsernameOrEmail("sun", "sun@example.com");
        verify(pendingRegistrationDao).insert(captor.capture());
        PendingRegistration pending = captor.getValue();
        assertEquals("sun", pending.getUsername());
        assertEquals("sun@example.com", pending.getEmail());
        assertEquals("registration-hash", pending.getTokenHash());
        assertTrue(passwordEncoder.matches("password123", pending.getPasswordHash()));
        assertNotNull(pending.getExpiresAt());
        verify(emailNotificationService).sendRegistrationConfirmation("sun@example.com", "registration-token");
    }

    @Test
    void confirmRegistrationCreatesUserAndRefreshToken() {
        PendingRegistration pending = new PendingRegistration();
        pending.setId(10L);
        pending.setUsername("sun");
        pending.setEmail("sun@example.com");
        pending.setPasswordHash(passwordEncoder.encode("password123"));
        pending.setTokenHash("registration-hash");
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(secureTokenService.hash("registration-token")).thenReturn("registration-hash");
        when(pendingRegistrationDao.selectOne(any(Wrapper.class))).thenReturn(pending);
        when(userDao.findByUsername("sun")).thenReturn(null);
        when(userDao.findByEmail("sun@example.com")).thenReturn(null);
        doAnswer(invocation -> {
            UserAccount inserted = invocation.getArgument(0);
            inserted.setId(42L);
            return 1;
        }).when(userDao).insert(any(UserAccount.class));
        when(userDao.selectById(42L)).thenAnswer(invocation -> {
            UserAccount user = new UserAccount();
            user.setId(42L);
            user.setUsername("sun");
            user.setEmail("sun@example.com");
            user.setEmailVerifiedAt(LocalDateTime.now());
            user.setNickname("sun");
            user.setRole("user");
            user.setStatus("active");
            user.setTokenVersion(0);
            return user;
        });
        when(secureTokenService.generateToken()).thenReturn("refresh-token");
        when(secureTokenService.hash("refresh-token")).thenReturn("refresh-hash");
        when(jwtService.generateToken("sun", "user", 0)).thenReturn("access-token");

        AuthSession session = authService.confirmRegistration("registration-token", "127.0.0.1", "JUnit");

        assertEquals("access-token", session.response().token());
        assertEquals("refresh-token", session.refreshToken());
        assertNotNull(pending.getConsumedAt());
        verify(pendingRegistrationDao).updateById(pending);
        verify(authRefreshTokenDao).insert(any(AuthRefreshToken.class));
    }

    @Test
    void refreshRotatesRefreshToken() {
        AuthRefreshToken current = new AuthRefreshToken();
        current.setId(1L);
        current.setUserId(42L);
        current.setTokenHash("old-hash");
        current.setExpiresAt(LocalDateTime.now().plusDays(1));

        UserAccount user = new UserAccount();
        user.setId(42L);
        user.setUsername("sun");
        user.setRole("admin");
        user.setStatus("active");
        user.setTokenVersion(3);

        when(secureTokenService.hash("old-refresh")).thenReturn("old-hash");
        when(authRefreshTokenDao.selectOne(any(Wrapper.class))).thenReturn(current);
        when(userDao.selectById(42L)).thenReturn(user);
        when(secureTokenService.generateToken()).thenReturn("new-refresh");
        when(secureTokenService.hash("new-refresh")).thenReturn("new-hash");
        when(jwtService.generateToken("sun", "admin", 3)).thenReturn("new-access");

        AuthSession session = authService.refresh("old-refresh", "127.0.0.1", "JUnit");

        assertEquals("new-access", session.response().token());
        assertEquals("new-refresh", session.refreshToken());
        assertNotNull(current.getRevokedAt());
        verify(authRefreshTokenDao).updateById(current);
        verify(authRefreshTokenDao).insert(any(AuthRefreshToken.class));
    }

    @Test
    void forgotPasswordDoesNotRevealUnknownEmail() {
        when(userDao.findByEmail("missing@example.com")).thenReturn(null);

        authService.requestPasswordReset(new PasswordForgotRequest("missing@example.com"), "127.0.0.1");

        verify(passwordResetTokenDao, never()).insert(any(PasswordResetToken.class));
        verify(emailNotificationService, never()).sendPasswordReset(eq("missing@example.com"), any());
    }
}
