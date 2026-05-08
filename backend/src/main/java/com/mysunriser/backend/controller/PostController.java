package com.mysunriser.backend.controller;


import com.mysunriser.backend.dto.CreatePostRequest;
import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.service.postservice;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blog")
public class PostController {

    private final postservice postservice;

    public PostController(postservice postservice) {
        this.postservice = postservice;
    }

    @GetMapping("/{slug}")
    public PostResponse getPost(@PathVariable String slug, Authentication authentication){
        return postservice.getPostBySlug(slug, authentication);
    }

    @PostMapping
    public String createPost(@Valid @RequestBody CreatePostRequest request){
        return postservice.initPost(request.toEntity());
    }
}
