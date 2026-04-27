package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.dto.CreatePostRequest;
import com.mysunriser.backend.dto.UpdatePostRequest;
import com.mysunriser.backend.service.postservice;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final postservice postservice;

    public AdminPostController(postservice postservice) {
        this.postservice = postservice;
    }

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postservice.createPost(request);
    }

    @PutMapping("/{slug}")
    public PostResponse updatePost(@PathVariable String slug, @Valid @RequestBody UpdatePostRequest request) {
        return postservice.updatePost(slug, request);
    }
}
