package com.github.dgaponov99.practicum.myblog.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostPageDTO {

    private List<PostDTO> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;

}
