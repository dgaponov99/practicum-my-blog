package com.github.dgaponov99.practicum.myblog.persistence.repository;

import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PostRepository {

    List<Post> findAll(String title, Set<String> tags, int size, int offset);

    int count(String title, Set<String> tags);

    Optional<Post> findById(Long id);

    Post create(String title, String text, Set<String> tags);

    void update(Long id, String title, String text, Set<String> tags);

    void updateImageUuid(Long id, UUID imageUuid);

    void incrementLikes(Long id);

    void deleteById(Long id);

}
