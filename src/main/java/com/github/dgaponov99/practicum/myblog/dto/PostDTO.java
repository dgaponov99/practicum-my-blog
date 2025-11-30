package com.github.dgaponov99.practicum.myblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private long id;
    private String title;
    private String text;
    private Set<String> tags;
    private int likesCount;
    private int commentsCount;

}
