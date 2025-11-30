package com.github.dgaponov99.practicum.myblog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPageDTO {

    private List<PostDTO> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;

}
