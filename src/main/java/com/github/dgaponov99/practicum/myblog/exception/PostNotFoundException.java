package com.github.dgaponov99.practicum.myblog.exception;

public class PostNotFoundException extends Exception {

    public PostNotFoundException(long postId) {
        super("Post with id %d does not exist".formatted(postId));
    }

}
