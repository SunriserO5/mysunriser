package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AdminCreateUserRequest;
import com.mysunriser.backend.dto.AdminResetPasswordRequest;
import com.mysunriser.backend.dto.AdminUserItemResponse;
import com.mysunriser.backend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserItemResponse> listUsers() {
        return adminUserService.listUsers();
    }

    @PostMapping
    public AdminUserItemResponse createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PostMapping("/{id}/reset-password")
    public Map<String, String> resetPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminResetPasswordRequest request
    ) {
        adminUserService.resetPassword(id, request.newPassword());
        return Map.of("message", "ok");
    }

    @PostMapping("/{id}/disable")
    public AdminUserItemResponse disableUser(@PathVariable("id") Long id) {
        return adminUserService.updateUserStatus(id, false);
    }

    @PostMapping("/{id}/enable")
    public AdminUserItemResponse enableUser(@PathVariable("id") Long id) {
        return adminUserService.updateUserStatus(id, true);
    }
}
