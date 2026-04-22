package com.mysunriser.backend.controller;


import com.mysunriser.backend.dto.CreatePostRequest;
import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.service.postservice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blog")
public class PostController {

    private final postservice postservice;

    public PostController(postservice postservice) {
        this.postservice = postservice;
    }

    @GetMapping("/{slug}")
    public PostResponse getPost(@PathVariable String slug){
        return postservice.getPostBySlug(slug);
    }

    @PostMapping
    public String createPost(@RequestBody CreatePostRequest request){
        return postservice.initPost(request.toEntity());
    }
}
