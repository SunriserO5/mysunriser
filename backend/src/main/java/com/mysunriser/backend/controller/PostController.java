package com.mysunriser.backend.controller;


import com.mysunriser.backend.dto.PostResponse;
import com.mysunriser.backend.service.postservice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
