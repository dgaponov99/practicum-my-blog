package com.github.dgaponov99.practicum.myblog.dto.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDataDTO {

    @NotBlank(message = "Отсутствует заголовок поста")
    private String title;
    @NotBlank(message = "Отсутствует тело поста")
    private String text;
    private Set<String> tags = new HashSet<>();

}
