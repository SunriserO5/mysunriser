package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AdminProjectRequest;
import com.mysunriser.backend.dto.ProjectListResponse;
import com.mysunriser.backend.dto.ProjectResponse;
import com.mysunriser.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectController {
    private final ProjectService projectService;

    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ProjectListResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return projectService.listAdmin(page, pageSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody AdminProjectRequest request) {
        return projectService.create(request);
    }

    @PutMapping("/{slug}")
    public ProjectResponse update(@PathVariable String slug, @Valid @RequestBody AdminProjectRequest request) {
        return projectService.update(slug, request);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug) {
        projectService.delete(slug);
    }
}
