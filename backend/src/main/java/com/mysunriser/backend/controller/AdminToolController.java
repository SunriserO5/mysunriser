package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.AdminToolRequest;
import com.mysunriser.backend.dto.ToolListResponse;
import com.mysunriser.backend.dto.ToolResponse;
import com.mysunriser.backend.service.ToolService;
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
@RequestMapping("/api/admin/tools")
public class AdminToolController {
    private final ToolService toolService;

    public AdminToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public ToolListResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return toolService.listAdmin(page, pageSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToolResponse create(@Valid @RequestBody AdminToolRequest request) {
        return toolService.create(request);
    }

    @PutMapping("/{slug}")
    public ToolResponse update(@PathVariable String slug, @Valid @RequestBody AdminToolRequest request) {
        return toolService.update(slug, request);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug) {
        toolService.delete(slug);
    }
}
