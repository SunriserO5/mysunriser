package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mysunriser.backend.Dao.UserDao;
import com.mysunriser.backend.dto.AdminCreateUserRequest;
import com.mysunriser.backend.dto.AdminUserItemResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.entity.UserAccount;
import com.mysunriser.backend.exception.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdminUserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserItemResponse> listUsers() {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<UserAccount>()
                .orderByDesc(UserAccount::getCreatedAt);

        List<UserAccount> users = userDao.selectList(wrapper);
        return users.stream().map(this::toResponse).toList();
    }

    public AdminUserItemResponse createUser(AdminCreateUserRequest request) {
        String username = request.username().trim();
        UserAccount existingUser = userDao.findByUsername(username);
        if (existingUser != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(normalizeRole(request.role()));
        user.setStatus("active");
        user.setTokenVersion(0);

        userDao.insert(user);
        return toResponse(userDao.selectById(user.getId()));
    }

    public void resetPassword(Long userId, String newPassword) {
        UserAccount user = userDao.selectById(userId);
        if (user == null) {
            throw new BizException(Codes.NOT_FOUND, "user not found");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(nextTokenVersion(user));
        userDao.updateById(user);
    }

    public AdminUserItemResponse updateUserStatus(Long userId, boolean enabled) {
        UserAccount user = userDao.selectById(userId);
        if (user == null) {
            throw new BizException(Codes.NOT_FOUND, "user not found");
        }

        user.setStatus(enabled ? "active" : "disabled");
        userDao.updateById(user);
        return toResponse(user);
    }

    private AdminUserItemResponse toResponse(UserAccount user) {
        return new AdminUserItemResponse(
                user.getId(),
                user.getUsername(),
                normalizeRole(user.getRole()),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "user";
        }

        String normalized = role.toLowerCase(Locale.ROOT);
        if (!"admin".equals(normalized) && !"user".equals(normalized)) {
            throw new BizException(Codes.VALIDATION_ERROR, "role must be admin or user");
        }

        return normalized;
    }

    private int nextTokenVersion(UserAccount user) {
        return (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1;
    }
}
