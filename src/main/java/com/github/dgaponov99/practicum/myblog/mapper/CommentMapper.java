package com.github.dgaponov99.practicum.myblog.mapper;

import com.github.dgaponov99.practicum.myblog.configuration.MapstructConfiguration;
import com.github.dgaponov99.practicum.myblog.dto.CommentDTO;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public abstract class CommentMapper {

    public abstract CommentDTO toDto(Comment comment);

}
