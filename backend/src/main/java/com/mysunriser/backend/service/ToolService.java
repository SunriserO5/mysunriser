package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.OnlineToolDao;
import com.mysunriser.backend.dto.AdminToolRequest;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.ToolAccessLevel;
import com.mysunriser.backend.dto.ToolEntryType;
import com.mysunriser.backend.dto.ToolListResponse;
import com.mysunriser.backend.dto.ToolResponse;
import com.mysunriser.backend.dto.ToolStatus;
import com.mysunriser.backend.entity.OnlineTool;
import com.mysunriser.backend.exception.BizException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ToolService {
    private final OnlineToolDao onlineToolDao;

    public ToolService(OnlineToolDao onlineToolDao) {
        this.onlineToolDao = onlineToolDao;
    }

    public ToolListResponse listVisible(int pageNum, int pageSize, Authentication authentication) {
        int safePage = Math.max(1, pageNum);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        Page<OnlineTool> page = new Page<>(safePage, safePageSize);

        LambdaQueryWrapper<OnlineTool> wrapper = baseListWrapper()
                .eq(OnlineTool::getStatus, ToolStatus.PUBLISHED.value())
                .in(OnlineTool::getAccessLevel, readableAccessLevels(authentication));

        Page<OnlineTool> result = onlineToolDao.selectPage(page, wrapper);
        return toListResponse(safePage, safePageSize, result);
    }

    public ToolResponse getVisibleBySlug(String slug, Authentication authentication) {
        OnlineTool tool = findBySlug(slug);
        if (!ToolStatus.PUBLISHED.value().equals(tool.getStatus())) {
            throw new BizException(Codes.NOT_FOUND, "tool not found");
        }

        assertCanRead(tool, authentication);
        return ToolResponse.of(tool);
    }

    public ToolListResponse listAdmin(int pageNum, int pageSize) {
        int safePage = Math.max(1, pageNum);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        Page<OnlineTool> page = new Page<>(safePage, safePageSize);
        Page<OnlineTool> result = onlineToolDao.selectPage(page, baseListWrapper());
        return toListResponse(safePage, safePageSize, result);
    }

    @Transactional
    public ToolResponse create(AdminToolRequest request) {
        String slug = normalizeSlug(request.slug());
        if (selectBySlug(slug) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "tool slug already exists");
        }

        OnlineTool tool = new OnlineTool();
        applyRequest(tool, request, slug);

        if (onlineToolDao.insert(tool) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "tool create failed");
        }

        return ToolResponse.of(findBySlug(slug));
    }

    @Transactional
    public ToolResponse update(String slug, AdminToolRequest request) {
        OnlineTool existing = findBySlug(slug);
        String nextSlug = normalizeSlug(request.slug());
        OnlineTool duplicate = selectBySlug(nextSlug);
        if (duplicate != null && !duplicate.getId().equals(existing.getId())) {
            throw new BizException(Codes.VALIDATION_ERROR, "tool slug already exists");
        }

        applyRequest(existing, request, nextSlug);
        if (onlineToolDao.updateById(existing) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "tool update failed");
        }

        return ToolResponse.of(findBySlug(nextSlug));
    }

    @Transactional
    public void delete(String slug) {
        OnlineTool existing = findBySlug(slug);
        if (onlineToolDao.deleteById(existing.getId()) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "tool delete failed");
        }
    }

    private ToolListResponse toListResponse(int page, int pageSize, Page<OnlineTool> result) {
        List<ToolResponse> items = result.getRecords().stream()
                .map(ToolResponse::of)
                .toList();

        return new ToolListResponse(page, pageSize, result.getTotal(), items);
    }

    private LambdaQueryWrapper<OnlineTool> baseListWrapper() {
        return new LambdaQueryWrapper<OnlineTool>()
                .orderByAsc(OnlineTool::getSortOrder)
                .orderByDesc(OnlineTool::getCreatedAt)
                .orderByDesc(OnlineTool::getId);
    }

    private void applyRequest(OnlineTool tool, AdminToolRequest request, String slug) {
        ToolStatus status = parseStatus(request.status());
        ToolEntryType entryType = parseEntryType(request.entryType());
        ToolAccessLevel accessLevel = parseAccessLevel(request.accessLevel());

        String routePath = normalizeRoutePath(request.routePath());
        String externalUrl = normalizeExternalUrl(request.externalUrl());

        if (entryType == ToolEntryType.INTERNAL && routePath.isBlank()) {
            throw new BizException(Codes.VALIDATION_ERROR, "routePath is required for internal tools");
        }

        if (entryType == ToolEntryType.EXTERNAL && externalUrl.isBlank()) {
            throw new BizException(Codes.VALIDATION_ERROR, "externalUrl is required for external tools");
        }

        tool.setSlug(slug);
        tool.setTitle(request.title().trim());
        tool.setSummary(request.summary().trim());
        tool.setStatus(status.value());
        tool.setEntryType(entryType.name());
        tool.setRoutePath(entryType == ToolEntryType.INTERNAL ? routePath : "");
        tool.setExternalUrl(entryType == ToolEntryType.EXTERNAL ? externalUrl : "");
        tool.setAccessLevel(accessLevel.name());
        tool.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private OnlineTool findBySlug(String slug) {
        OnlineTool tool = selectBySlug(normalizeSlug(slug));
        if (tool == null) {
            throw new BizException(Codes.NOT_FOUND, "tool not found");
        }

        return tool;
    }

    private OnlineTool selectBySlug(String slug) {
        return onlineToolDao.selectOne(new LambdaQueryWrapper<OnlineTool>()
                .eq(OnlineTool::getSlug, slug)
                .last("LIMIT 1"));
    }

    private String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim();
    }

    private String normalizeRoutePath(String routePath) {
        String normalized = routePath == null ? "" : routePath.trim();
        if (normalized.isBlank()) {
            return "";
        }

        if (!normalized.startsWith("/tools/") || normalized.contains("://")) {
            throw new BizException(Codes.VALIDATION_ERROR, "routePath must start with /tools/");
        }

        return normalized;
    }

    private String normalizeExternalUrl(String externalUrl) {
        String normalized = externalUrl == null ? "" : externalUrl.trim();
        if (normalized.isBlank()) {
            return "";
        }

        if (!isHttpUrl(normalized)) {
            throw new BizException(Codes.VALIDATION_ERROR, "externalUrl must be an http or https URL");
        }

        return normalized;
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private ToolStatus parseStatus(String value) {
        try {
            return ToolStatus.from(value);
        } catch (IllegalArgumentException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid tool status");
        }
    }

    private ToolEntryType parseEntryType(String value) {
        try {
            return ToolEntryType.from(value);
        } catch (IllegalArgumentException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid tool entry type");
        }
    }

    private ToolAccessLevel parseAccessLevel(String value) {
        try {
            return ToolAccessLevel.from(value);
        } catch (IllegalArgumentException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid tool access level");
        }
    }

    private void assertCanRead(OnlineTool tool, Authentication authentication) {
        ToolAccessLevel accessLevel = parseAccessLevel(tool.getAccessLevel());
        if (accessLevel == ToolAccessLevel.PUBLIC) {
            return;
        }

        if (accessLevel == ToolAccessLevel.AUTHENTICATED && !isRealAuthenticated(authentication)) {
            throw new BizException(Codes.UNAUTHORIZED, "login required");
        }

        if (accessLevel == ToolAccessLevel.ADMIN && !isAdmin(authentication)) {
            throw new BizException(isRealAuthenticated(authentication) ? Codes.FORBIDDEN : Codes.UNAUTHORIZED, "admin required");
        }
    }

    private List<String> readableAccessLevels(Authentication authentication) {
        List<String> levels = new ArrayList<>();
        levels.add(ToolAccessLevel.PUBLIC.name());

        if (isRealAuthenticated(authentication)) {
            levels.add(ToolAccessLevel.AUTHENTICATED.name());
        }

        if (isAdmin(authentication)) {
            levels.add(ToolAccessLevel.ADMIN.name());
        }

        return levels;
    }

    private boolean isAdmin(Authentication authentication) {
        return isRealAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isRealAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
