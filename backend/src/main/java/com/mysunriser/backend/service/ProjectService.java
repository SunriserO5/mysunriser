package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.ProjectDao;
import com.mysunriser.backend.dto.AdminProjectRequest;
import com.mysunriser.backend.dto.Codes;
import com.mysunriser.backend.dto.ProjectListResponse;
import com.mysunriser.backend.dto.ProjectResponse;
import com.mysunriser.backend.dto.ProjectStatus;
import com.mysunriser.backend.entity.Project;
import com.mysunriser.backend.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectDao projectDao;
    private final GitHubReadmeService gitHubReadmeService;
    private final Duration readmeCacheTtl;

    public ProjectService(
            ProjectDao projectDao,
            GitHubReadmeService gitHubReadmeService,
            @Value("${projects.readme-cache-hours:6}") long readmeCacheHours
    ) {
        this.projectDao = projectDao;
        this.gitHubReadmeService = gitHubReadmeService;
        this.readmeCacheTtl = Duration.ofHours(Math.max(1, readmeCacheHours));
    }

    public ProjectListResponse listVisible(int pageNum, int pageSize) {
        int safePage = Math.max(1, pageNum);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        Page<Project> page = new Page<>(safePage, safePageSize);
        Page<Project> result = projectDao.selectPage(page, baseListWrapper()
                .eq(Project::getStatus, ProjectStatus.PUBLISHED.value()));

        return toListResponse(safePage, safePageSize, result);
    }

    public ProjectResponse getVisibleBySlug(String slug) {
        Project project = findBySlug(slug);
        if (!ProjectStatus.PUBLISHED.value().equals(project.getStatus())) {
            throw new BizException(Codes.NOT_FOUND, "project not found");
        }

        ProjectReadmeView readme = refreshReadme(project);
        return ProjectResponse.of(project, readme.markdown(), readme.error());
    }

    public ProjectListResponse listAdmin(int pageNum, int pageSize) {
        int safePage = Math.max(1, pageNum);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        Page<Project> page = new Page<>(safePage, safePageSize);
        Page<Project> result = projectDao.selectPage(page, baseListWrapper());
        return toListResponse(safePage, safePageSize, result);
    }

    @Transactional
    public ProjectResponse create(AdminProjectRequest request) {
        String slug = normalizeSlug(request.slug());
        if (selectBySlug(slug) != null) {
            throw new BizException(Codes.VALIDATION_ERROR, "project slug already exists");
        }

        Project project = new Project();
        applyRequest(project, request, slug);

        if (projectDao.insert(project) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "project create failed");
        }

        return ProjectResponse.of(findBySlug(slug));
    }

    @Transactional
    public ProjectResponse update(String slug, AdminProjectRequest request) {
        Project existing = findBySlug(slug);
        String nextSlug = normalizeSlug(request.slug());
        Project duplicate = selectBySlug(nextSlug);
        if (duplicate != null && !duplicate.getId().equals(existing.getId())) {
            throw new BizException(Codes.VALIDATION_ERROR, "project slug already exists");
        }

        String previousOwner = existing.getRepoOwner();
        String previousName = existing.getRepoName();
        applyRequest(existing, request, nextSlug);
        if (!existing.getRepoOwner().equals(previousOwner) || !existing.getRepoName().equals(previousName)) {
            existing.setReadmeMarkdown(null);
            existing.setReadmeEtag(null);
            existing.setReadmeCachedAt(null);
        }

        if (projectDao.updateById(existing) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "project update failed");
        }

        return ProjectResponse.of(findBySlug(nextSlug));
    }

    @Transactional
    public void delete(String slug) {
        Project existing = findBySlug(slug);
        if (projectDao.deleteById(existing.getId()) <= 0) {
            throw new BizException(Codes.INTERNAL_ERROR, "project delete failed");
        }
    }

    private ProjectListResponse toListResponse(int page, int pageSize, Page<Project> result) {
        List<ProjectResponse> items = result.getRecords().stream()
                .map(ProjectResponse::listItem)
                .toList();

        return new ProjectListResponse(page, pageSize, result.getTotal(), items);
    }

    private ProjectReadmeView refreshReadme(Project project) {
        if (hasFreshReadme(project)) {
            return new ProjectReadmeView(project.getReadmeMarkdown(), null);
        }

        GitHubReadmeResult result = gitHubReadmeService.fetchReadme(
                project.getRepoOwner(),
                project.getRepoName(),
                project.getReadmeEtag()
        );

        if (result.fetched()) {
            project.setReadmeMarkdown(result.markdown());
            project.setReadmeEtag(result.etag());
            project.setReadmeCachedAt(LocalDateTime.now());
            projectDao.updateById(project);
            return new ProjectReadmeView(project.getReadmeMarkdown(), null);
        }

        if (result.error() == null) {
            project.setReadmeCachedAt(LocalDateTime.now());
            projectDao.updateById(project);
            return new ProjectReadmeView(project.getReadmeMarkdown(), null);
        }

        return new ProjectReadmeView(project.getReadmeMarkdown(), result.error());
    }

    private boolean hasFreshReadme(Project project) {
        if (project.getReadmeMarkdown() == null || project.getReadmeCachedAt() == null) {
            return false;
        }

        return project.getReadmeCachedAt().plus(readmeCacheTtl).isAfter(LocalDateTime.now());
    }

    private LambdaQueryWrapper<Project> baseListWrapper() {
        return new LambdaQueryWrapper<Project>()
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getCreatedAt)
                .orderByDesc(Project::getId);
    }

    private void applyRequest(Project project, AdminProjectRequest request, String slug) {
        ProjectStatus status = parseStatus(request.status());
        String owner = request.repoOwner().trim();
        String repoName = request.repoName().trim();

        project.setSlug(slug);
        project.setTitle(request.title().trim());
        project.setSummary(request.summary().trim());
        project.setStatus(status.value());
        project.setRepoOwner(owner);
        project.setRepoName(repoName);
        project.setRepoUrl(normalizeRepoUrl(request.repoUrl(), owner, repoName));
        project.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private Project findBySlug(String slug) {
        Project project = selectBySlug(normalizeSlug(slug));
        if (project == null) {
            throw new BizException(Codes.NOT_FOUND, "project not found");
        }

        return project;
    }

    private Project selectBySlug(String slug) {
        return projectDao.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getSlug, slug)
                .last("LIMIT 1"));
    }

    private String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim();
    }

    private String normalizeRepoUrl(String repoUrl, String owner, String repoName) {
        String normalized = repoUrl == null ? "" : repoUrl.trim();
        if (normalized.isBlank()) {
            return "https://github.com/%s/%s".formatted(owner, repoName);
        }

        if (!isHttpUrl(normalized)) {
            throw new BizException(Codes.VALIDATION_ERROR, "repoUrl must be an http or https URL");
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

    private ProjectStatus parseStatus(String value) {
        try {
            return ProjectStatus.from(value);
        } catch (IllegalArgumentException e) {
            throw new BizException(Codes.VALIDATION_ERROR, "invalid project status");
        }
    }
}
