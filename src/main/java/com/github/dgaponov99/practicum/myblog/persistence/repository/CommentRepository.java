package com.github.dgaponov99.practicum.myblog.persistence.repository;

import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(Long id);

    int countByPostId(Long postId);

    List<Comment> findByPostId(Long postId);

    Comment create(Long postId, String text);

    void update(Long id, String text);

    void deleteById(Long id);

}
