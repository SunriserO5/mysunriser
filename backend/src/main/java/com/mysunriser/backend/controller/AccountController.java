package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AccountEmailChangeRequest;
import com.mysunriser.backend.dto.AccountPasswordUpdateRequest;
import com.mysunriser.backend.dto.AccountProfileResponse;
import com.mysunriser.backend.dto.AccountProfileUpdateRequest;
import com.mysunriser.backend.dto.AuthMessageResponse;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.exception.BizException;
import com.mysunriser.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/profile")
    public AccountProfileResponse profile(Authentication authentication) {
        return accountService.profile(requireUsername(authentication));
    }

    @PutMapping("/profile")
    public AccountProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody AccountProfileUpdateRequest request
    ) {
        return accountService.updateProfile(requireUsername(authentication), request);
    }

    @PutMapping("/password")
    public AuthMessageResponse updatePassword(
            Authentication authentication,
            @Valid @RequestBody AccountPasswordUpdateRequest request
    ) {
        accountService.changePassword(requireUsername(authentication), request);
        return new AuthMessageResponse("ok");
    }

    @PutMapping("/email")
    public AuthMessageResponse requestEmailChange(
            Authentication authentication,
            @Valid @RequestBody AccountEmailChangeRequest request
    ) {
        accountService.requestEmailChange(requireUsername(authentication), request);
        return new AuthMessageResponse("ok");
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(Codes.UNAUTHORIZED, "unauthorized");
        }

        return authentication.getName();
    }
}
