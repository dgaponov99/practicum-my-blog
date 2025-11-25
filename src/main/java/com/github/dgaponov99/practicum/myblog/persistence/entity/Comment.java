package com.github.dgaponov99.practicum.myblog.persistence.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @EqualsAndHashCode.Include
    private Long id;
    private long postId;
    private String text;

    private boolean deleted;

}
