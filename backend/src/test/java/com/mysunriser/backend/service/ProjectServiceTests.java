package com.mysunriser.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysunriser.backend.Dao.ProjectDao;
import com.mysunriser.backend.dto.AdminProjectRequest;
import com.mysunriser.backend.dto.ProjectResponse;
import com.mysunriser.backend.entity.Project;
import com.mysunriser.backend.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTests {
    @Mock
    private ProjectDao projectDao;

    @Mock
    private GitHubReadmeService gitHubReadmeService;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectDao, gitHubReadmeService, 6);
    }

    @Test
    void createDefaultsRepoUrlAndStatus() {
        when(projectDao.selectOne(any(Wrapper.class))).thenReturn(null, savedProject());
        when(projectDao.insert(any(Project.class))).thenReturn(1);

        ProjectResponse response = projectService.create(new AdminProjectRequest(
                "demo-project",
                "Demo Project",
                "A small demo",
                "",
                "SunRiser",
                "mysunriser",
                "",
                null
        ));

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectDao).insert(captor.capture());
        Project inserted = captor.getValue();
        assertEquals("Draft", inserted.getStatus());
        assertEquals("https://github.com/SunRiser/mysunriser", inserted.getRepoUrl());
        assertEquals(0, inserted.getSortOrder());
        assertEquals("demo-project", response.slug());
    }

    @Test
    void createRejectsInvalidRepoUrl() {
        when(projectDao.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThrows(BizException.class, () -> projectService.create(new AdminProjectRequest(
                "demo-project",
                "Demo Project",
                "A small demo",
                "Published",
                "SunRiser",
                "mysunriser",
                "ftp://example.com/repo",
                0
        )));

        verify(projectDao, never()).insert(any(Project.class));
    }

    @Test
    void detailUsesCachedReadmeWhenFresh() {
        Project project = savedProject();
        project.setReadmeMarkdown("# Cached README");
        project.setReadmeCachedAt(LocalDateTime.now());
        when(projectDao.selectOne(any(Wrapper.class))).thenReturn(project);

        ProjectResponse response = projectService.getVisibleBySlug("demo-project");

        assertEquals("# Cached README", response.readmeMarkdown());
        assertEquals("", response.readmeError());
        verify(gitHubReadmeService, never()).fetchReadme(any(), any(), any());
    }

    @Test
    void detailRefreshesReadmeOnNotModified() {
        Project project = savedProject();
        project.setReadmeMarkdown("# Cached README");
        project.setReadmeEtag("\"etag\"");
        project.setReadmeCachedAt(LocalDateTime.now().minusHours(8));
        when(projectDao.selectOne(any(Wrapper.class))).thenReturn(project);
        when(gitHubReadmeService.fetchReadme("SunRiser", "mysunriser", "\"etag\""))
                .thenReturn(GitHubReadmeResult.notModified());
        when(projectDao.updateById(project)).thenReturn(1);

        ProjectResponse response = projectService.getVisibleBySlug("demo-project");

        assertEquals("# Cached README", response.readmeMarkdown());
        assertEquals("", response.readmeError());
        assertNotNull(project.getReadmeCachedAt());
        verify(projectDao).updateById(project);
    }

    @Test
    void detailReturnsStaleReadmeAndErrorWhenFetchFails() {
        Project project = savedProject();
        project.setReadmeMarkdown("# Stale README");
        project.setReadmeCachedAt(LocalDateTime.now().minusHours(8));
        when(projectDao.selectOne(any(Wrapper.class))).thenReturn(project);
        when(gitHubReadmeService.fetchReadme("SunRiser", "mysunriser", null))
                .thenReturn(GitHubReadmeResult.failed("GitHub 请求受限，请稍后再试"));

        ProjectResponse response = projectService.getVisibleBySlug("demo-project");

        assertEquals("# Stale README", response.readmeMarkdown());
        assertEquals("GitHub 请求受限，请稍后再试", response.readmeError());
    }

    @Test
    void listVisibleReturnsListItemsWithoutReadmeBody() {
        Page<Project> page = new Page<>(1, 20);
        page.setRecords(List.of(savedProject()));
        page.setTotal(1);
        when(projectDao.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        ProjectResponse item = projectService.listVisible(1, 20).items().getFirst();

        assertEquals("demo-project", item.slug());
        assertEquals("", item.readmeMarkdown());
    }

    private Project savedProject() {
        Project project = new Project();
        project.setId(1L);
        project.setSlug("demo-project");
        project.setTitle("Demo Project");
        project.setSummary("A small demo");
        project.setStatus("Published");
        project.setRepoOwner("SunRiser");
        project.setRepoName("mysunriser");
        project.setRepoUrl("https://github.com/SunRiser/mysunriser");
        project.setSortOrder(0);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }
}
