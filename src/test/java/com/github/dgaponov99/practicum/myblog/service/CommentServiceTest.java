package com.github.dgaponov99.practicum.myblog.service;


import com.github.dgaponov99.practicum.myblog.config.ServiceTestConfig;
import com.github.dgaponov99.practicum.myblog.dto.CommentDTO;
import com.github.dgaponov99.practicum.myblog.dto.data.CommentDataDTO;
import com.github.dgaponov99.practicum.myblog.exception.CommentNotFoundException;
import com.github.dgaponov99.practicum.myblog.exception.PostNotFoundException;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {ServiceTestConfig.class})
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    public void setUp() {
        reset(postRepository, commentRepository);
    }

    @Test
    void getComment_success() {
        var commentId = 1L;
        var postId = 1L;
        var commentText = "Комментарий";

        var expectedCommentDto = new CommentDTO(commentId, postId, commentText);

        var comment = new Comment(commentId, postId, commentText, false);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        var commentDto = commentService.getComment(commentId).orElseThrow();

        assertEquals(expectedCommentDto, commentDto);
        verify(commentRepository, times(1)).findById(commentId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getComment_notFound() {
        var commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        var commentDtoOpt = commentService.getComment(commentId);

        assertTrue(commentDtoOpt.isEmpty());
        verify(commentRepository, times(1)).findById(commentId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getComment_deleted() {
        var commentId = 1L;
        var postId = 1L;
        var commentText = "Комментарий";

        var comment = new Comment(commentId, postId, commentText, true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        var commentDtoOpt = commentService.getComment(commentId);

        assertTrue(commentDtoOpt.isEmpty());
        verify(commentRepository, times(1)).findById(commentId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getByPostId_success() {
        var postId = 1L;

        var comments = new ArrayList<Comment>(4);
        var expectCommentDTOs = new ArrayList<CommentDTO>(3);
        for (int i = 1; i <= 3; i++) {
            comments.add(new Comment((long) i, postId, "Комментарий " + i, false));
            expectCommentDTOs.add(new CommentDTO(i, postId, "Комментарий " + i));
        }
        comments.add(new Comment(4L, postId, "Комментарий " + 4, true));

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(postId, "Заголовок", "Текст", 0, null, Collections.emptySet(), false)));
        when(commentRepository.findByPostId(anyLong())).thenReturn(comments);

        assertDoesNotThrow(() -> {
            var commentDTOs = commentService.getByPostId(postId);
            assertEquals(expectCommentDTOs, commentDTOs);
        });
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).findByPostId(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getByPostId_empty() {
        var postId = 1L;

        var comments = new ArrayList<Comment>(1);
        comments.add(new Comment(4L, postId, "Комментарий " + 4, true));

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(postId, "Заголовок", "Текст", 0, null, Collections.emptySet(), false)));
        when(commentRepository.findByPostId(anyLong())).thenReturn(comments);

        assertDoesNotThrow(() -> {
            var commentDTOs = commentService.getByPostId(postId);
            assertTrue(commentDTOs.isEmpty());
        });
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).findByPostId(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void getByPostId_postNotFound() {
        var postId = 1L;

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> commentService.getByPostId(postId));
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(0)).findByPostId(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void createComment_success() {
        var commentId = 1L;
        var postId = 1L;
        var commentText = "Комментарий";
        var expectedCommentDto = new CommentDTO(commentId, postId, commentText);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post(postId, "Заголовок", "Текст", 0, null, Collections.emptySet(), false)));
        when(commentRepository.create(postId, commentText)).thenReturn(new Comment(commentId, postId, commentText, false));

        assertDoesNotThrow(() -> {
            var commentDto = commentService.createComment(postId, new CommentDataDTO(commentText));
            assertEquals(expectedCommentDto, commentDto);
        });

        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).create(postId, commentText);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void createComment_postNotFound() {
        var postId = 1L;
        var commentText = "Комментарий";

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> commentService.createComment(postId, new CommentDataDTO(commentText)));

        verify(postRepository, times(1)).findById(postId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void editComment_success() {
        var commentId = 1L;
        var postId = 1L;
        var newCommentText = "Новый комментарий";
        var expectedCommentDto = new CommentDTO(commentId, postId, newCommentText);

        doNothing().when(commentRepository).update(anyLong(), any());
        when(commentRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Comment(commentId, postId, "Старый комментарий", false)))
                .thenReturn(Optional.of(new Comment(commentId, postId, newCommentText, false)));

        assertDoesNotThrow(() -> {
            var commentDto = commentService.editComment(commentId, new CommentDataDTO(newCommentText));
            assertEquals(expectedCommentDto, commentDto);
        });

        verify(commentRepository, times(2)).findById(commentId);
        verify(commentRepository, times(1)).update(commentId, newCommentText);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void editComment_failNotFound() {
        var commentId = 1L;
        var newCommentText = "Новый комментарий";

        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.editComment(commentId, new CommentDataDTO(newCommentText)));

        verify(commentRepository, times(1)).findById(commentId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void deleteComment() {
        var commentId = 100500L;

        doNothing().when(commentRepository).deleteById(commentId);
        commentService.deleteComment(commentId);

        verify(commentRepository, times(1)).deleteById(commentId);
        verifyNoMoreInteractions(postRepository, commentRepository);
    }
}
