package com.github.dgaponov99.practicum.myblog.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDataDTO {

    @NotBlank(message = "Отсутствует текс комментария")
    private String text;

}
