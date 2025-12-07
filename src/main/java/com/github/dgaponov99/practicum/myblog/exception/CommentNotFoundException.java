package com.github.dgaponov99.practicum.myblog.exception;

public class CommentNotFoundException extends Exception {

    public CommentNotFoundException(long postId) {
        super("Comment with id %d does not exist".formatted(postId));
    }

}
