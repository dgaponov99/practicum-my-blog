package com.github.dgaponov99.practicum.myblog.dto;

import lombok.Data;

@Data
public class CommentDTO {

    private long id;
    private long postId;
    private String text;

}
