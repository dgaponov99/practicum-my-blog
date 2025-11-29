package com.github.dgaponov99.practicum.myblog.dto;

import lombok.Data;

import java.util.Set;

@Data
public class PostDTO {

    private long id;
    private String title;
    private String text;
    private Set<String> tags;
    private int likesCount;
    private int commentsCount;

}
