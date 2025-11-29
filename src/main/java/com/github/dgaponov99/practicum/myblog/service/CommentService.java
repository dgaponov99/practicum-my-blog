package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.dto.CommentDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.CommentDataDTO;
import com.github.dgaponov99.practicum.myblog.exception.CommentNotFoundException;
import com.github.dgaponov99.practicum.myblog.exception.PostNotFoundException;
import com.github.dgaponov99.practicum.myblog.mapper.CommentMapper;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;
import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostService postService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public Optional<CommentDTO> findById(long id) {
        return getNotDeleted(id).map(commentMapper::toDto);
    }

    public int countByPostId(long postId) throws PostNotFoundException {
        if (postService.getPost(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }
        return commentRepository.countByPostId(postId);
    }

    public List<CommentDTO> getByPostId(long postId) throws PostNotFoundException {
        if (postService.getPost(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }
        return commentRepository.findByPostId(postId).stream().filter(comment -> !comment.isDeleted()).map(commentMapper::toDto).toList();
    }

    public CommentDTO createComment(long postId, CommentDataDTO commentData) throws PostNotFoundException {
        if (postService.getPost(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }
        var comment = commentRepository.create(postId, commentData.getText());
        return commentMapper.toDto(comment);
    }

    public CommentDTO editComment(long id, CommentDataDTO commentData) throws CommentNotFoundException {
        getNotDeleted(id).orElseThrow(() -> new CommentNotFoundException(id));
        commentRepository.update(id, commentData.getText());
        return findById(id).orElseThrow();
    }

    public void deleteByPostId(long postId) throws PostNotFoundException {
        getByPostId(postId).forEach(comment -> {
            deleteComment(comment.getId());
        });
    }

    public void deleteComment(long id) {
        commentRepository.deleteById(id);
    }

    private Optional<Comment> getNotDeleted(long id) {
        return commentRepository.findById(id).filter(comment -> !comment.isDeleted());
    }

}
