package com.github.dgaponov99.practicum.myblog.dto.data;

import lombok.Data;

import java.util.Set;

@Data
public class PostDataDTO {

    private String title;
    private String text;
    private Set<String> tags;

}
