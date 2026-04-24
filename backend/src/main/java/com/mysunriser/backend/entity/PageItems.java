package com.mysunriser.backend.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PageItems {
    private String slug;
    private String title;
    private String summary;
    private String status;
    private LocalDateTime publishAt;

}
