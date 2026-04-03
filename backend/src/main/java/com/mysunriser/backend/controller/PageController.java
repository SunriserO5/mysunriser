package com.mysunriser.backend.controller;

import com.mysunriser.backend.dto.PageResponse;
import com.mysunriser.backend.service.postservice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mysunriser.backend.service.postservice;
@RestController
@RequestMapping("/api/page")
public class PageController {

    private final postservice postservice;
    public PageController(postservice postservice){this.postservice=postservice;}

    @GetMapping
    public PageResponse list(@RequestParam int page,
                             @RequestParam int pageSize) {
        return postservice.getPage(page,pageSize);
    }
}



