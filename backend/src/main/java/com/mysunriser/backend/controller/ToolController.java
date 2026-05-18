package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.ToolListResponse;
import com.mysunriser.backend.dto.ToolResponse;
import com.mysunriser.backend.service.ToolService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class ToolController {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public ToolListResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication authentication
    ) {
        return toolService.listVisible(page, pageSize, authentication);
    }

    @GetMapping("/{slug}")
    public ToolResponse detail(@PathVariable String slug, Authentication authentication) {
        return toolService.getVisibleBySlug(slug, authentication);
    }
}
