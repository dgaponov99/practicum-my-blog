package com.github.dgaponov99.practicum.myblog.repository;

import com.github.dgaponov99.practicum.myblog.PostgresRepositoryTest;
import com.github.dgaponov99.practicum.myblog.config.RepositoryITConfig;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;
import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.sql.Statement;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {RepositoryITConfig.class})
public class CommentRepositoryTest extends PostgresRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    CommentRepository commentRepository;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.update("truncate table posts, post_tag, comments restart identity cascade;");
    }

    @Test
    void findById_shouldReturnComment() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var commentId = insertComment(postId, "Комментарий", true);


        var comment = commentRepository.findById(postId).orElse(null);
        assertNotNull(comment);
        assertAll(
                () -> assertEquals(commentId, comment.getId()),
                () -> assertEquals("Комментарий", comment.getText()),
                () -> assertTrue(comment.isDeleted())
        );
    }

    @Test
    void findById_shouldReturnEmpty() {
        assertTrue(commentRepository.findById(100500L).isEmpty());
    }

    @Test
    void countByPostId_shouldReturnCommentCount() {
        var otherPostId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertComment(otherPostId, "Комментарий 1", false);
        insertComment(otherPostId, "Комментарий 2", true);
        insertComment(otherPostId, "Комментарий 3", false);

        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertComment(postId, "Комментарий 1", false);
        insertComment(postId, "Комментарий 2", false);
        insertComment(postId, "Комментарий 3", true);
        insertComment(postId, "Комментарий 4", false);

        assertEquals(3, commentRepository.countByPostId(postId));
    }

    @Test
    void countByPostId_shouldReturnZero() {
        var otherPostId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertComment(otherPostId, "Комментарий 1", false);
        insertComment(otherPostId, "Комментарий 2", true);
        insertComment(otherPostId, "Комментарий 3", false);

        assertEquals(0, commentRepository.countByPostId(100500L));
    }

    @Test
    void findByPostId_shouldReturnComments() {
        var otherPostId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertComment(otherPostId, "Комментарий 1", false);
        insertComment(otherPostId, "Комментарий 2", true);
        insertComment(otherPostId, "Комментарий 3", false);

        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var expectedCommentIds = new HashSet<Long>();
        expectedCommentIds.add(insertComment(postId, "Комментарий 1", false));
        expectedCommentIds.add(insertComment(postId, "Комментарий 2", false));
        insertComment(postId, "Комментарий 3", true);
        expectedCommentIds.add(insertComment(postId, "Комментарий 4", false));

        assertEquals(expectedCommentIds, commentRepository.findByPostId(postId).stream().map(Comment::getId).collect(Collectors.toSet()));
    }

    @Test
    void findByPostId_shouldReturnEmpty() {
        var otherPostId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertComment(otherPostId, "Комментарий 1", false);
        insertComment(otherPostId, "Комментарий 2", true);
        insertComment(otherPostId, "Комментарий 3", false);

        assertEquals(0, commentRepository.findByPostId(100500L).size());
    }

    @Test
    void create_shouldPersistComment() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);

        var comment = commentRepository.create(postId, "Комментарий");
        assertNotNull(comment);
        assertAll(
                () -> assertNotNull(comment.getId()),
                () -> assertEquals("Комментарий", comment.getText()),
                () -> assertFalse(comment.isDeleted())
        );

        var dbComment = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(dbComment);
        assertCommentEquals(comment, dbComment);
    }

    @Test
    void update_shouldUpdatePost() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var commentId = insertComment(postId, "Комментарий", false);

        commentRepository.update(postId, "Комментарий 2");
        var updatedComment = commentRepository.findById(commentId).orElse(null);
        assertNotNull(updatedComment);

        assertAll(
                () -> assertEquals("Комментарий 2", updatedComment.getText()),
                () -> assertFalse(updatedComment.isDeleted())
        );
    }

    @Test
    void update_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var commentId = insertComment(postId, "Комментарий", false);
        var beforeComment = commentRepository.findById(commentId).orElseThrow();

        commentRepository.update(100500L, "Комментарий 2");
        assertTrue(commentRepository.findById(100500L).isEmpty());

        var afterComment = commentRepository.findById(commentId).orElseThrow();

        assertCommentEquals(beforeComment, afterComment);
    }

    @Test
    void deleteById_shouldDeletePost() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var commentId = insertComment(postId, "Комментарий", false);

        commentRepository.deleteById(postId);

        var deletedComment = commentRepository.findById(commentId).orElse(null);
        assertNotNull(deletedComment);

        assertTrue(deletedComment.isDeleted());
    }

    @Test
    void deleteById_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        var commentId = insertComment(postId, "Комментарий", false);
        var beforeComment = commentRepository.findById(commentId).orElseThrow();

        commentRepository.deleteById(100500L);
        assertTrue(commentRepository.findById(100500L).isEmpty());

        var afterComment = commentRepository.findById(commentId).orElseThrow();

        assertCommentEquals(beforeComment, afterComment);
    }

    private void assertCommentEquals(Comment expected, Comment actual) {
        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getText(), actual.getText()),
                () -> assertEquals(expected.isDeleted(), actual.isDeleted())
        );
    }

    private long insertPost(String title, String text, int likesCount, UUID imageUuid, boolean deleted) {
        var postIdHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("""
                    insert into posts (title, text, likes_count, image_uuid, deleted)
                        values(?, ?, ?, ?, ?);
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, text);
            statement.setInt(3, likesCount);
            statement.setString(4, imageUuid != null ? imageUuid.toString() : null);
            statement.setBoolean(5, deleted);
            return statement;
        }, postIdHolder);
        return (long) postIdHolder.getKeys().get("post_id");
    }

    private long insertComment(Long postId, String text, boolean deleted) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("insert into comments (post_id, text, deleted) values (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, postId);
            statement.setString(2, text);
            statement.setBoolean(3, deleted);
            return statement;
        }, keyHolder);
        return (long) keyHolder.getKeys().get("comment_id");
    }
}
