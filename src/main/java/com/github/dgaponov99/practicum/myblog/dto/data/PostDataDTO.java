package com.github.dgaponov99.practicum.myblog.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDataDTO {

    private String title;
    private String text;
    private Set<String> tags;

}
