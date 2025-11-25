package com.github.dgaponov99.practicum.myblog.persistence.entity;


import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @EqualsAndHashCode.Include
    private Long id;
    private String title;
    private String text;
    private int likesCount;
    private UUID imageUuid;

    private Set<String> tags;

    boolean deleted;

}
