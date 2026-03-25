package com.mysunriser.backend.entity;

import lombok.Data;
@Data
public class post {
    private int id;
    private String slug;
    private String title;
    private String content;
    private String status;
    private int publishTime;
}
