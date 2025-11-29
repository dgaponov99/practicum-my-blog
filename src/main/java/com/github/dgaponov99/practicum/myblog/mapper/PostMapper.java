package com.github.dgaponov99.practicum.myblog.mapper;

import com.github.dgaponov99.practicum.myblog.configuration.MapstructConfiguration;
import com.github.dgaponov99.practicum.myblog.dto.PostDTO;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public abstract class PostMapper {

    public abstract PostDTO toDTO(Post post);

}
