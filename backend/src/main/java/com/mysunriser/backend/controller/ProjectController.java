package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.ProjectListResponse;
import com.mysunriser.backend.dto.ProjectResponse;
import com.mysunriser.backend.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ProjectListResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return projectService.listVisible(page, pageSize);
    }

    @GetMapping("/{slug}")
    public ProjectResponse detail(@PathVariable String slug) {
        return projectService.getVisibleBySlug(slug);
    }
}
