package com.github.dgaponov99.practicum.myblog;

import com.github.dgaponov99.practicum.myblog.configuration.DataSourceConfiguration;
import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Statement;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class})
public class PostRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @BeforeAll
    static void init() {
        postgres.start();
    }

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driverClassName", postgres::getDriverClassName);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostRepository postRepository;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.update("truncate table posts, post_tag restart identity cascade;");
    }

    @Test
    void findById_shouldReturnPost() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), true);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");

        var post = postRepository.findById(postId).orElse(null);
        assertNotNull(post);
        assertAll(
                () -> assertEquals(postId, post.getId()),
                () -> assertEquals("Заголовок 1", post.getTitle()),
                () -> assertEquals("Тело 1", post.getText()),
                () -> assertEquals(4, post.getLikesCount()),
                () -> assertEquals(UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), post.getImageUuid()),
                () -> assertEquals(Set.of("tag1", "tag2"), post.getTags()),
                () -> assertTrue(post.isDeleted())
        );
    }

    @Test
    void findById_shouldReturnEmpty() {
        assertTrue(postRepository.findById(100500L).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 10, 0",
            "100, 10, 10",
            "100, 100, 0",
            "100, 10, 100"
    })
    void search_allPosts(int totalCount, int size, int offset) {
        var postIds = new TreeSet<Long>();
        for (int i = 0; i < totalCount; i++) {
            var postId = insertPost("Заголовок %s".formatted(i), "Тело %s".formatted(i), i, null, false);
            insertPostTag(postId, "tag1");
            insertPostTag(postId, "tag2");
            postIds.add(postId);
        }
        var posts = postRepository.findAll(null, Collections.emptySet(), size, offset);
        var count = postRepository.count(null, Collections.emptySet());
        assertEquals(totalCount, count);
        assertIterableEquals(postIds.descendingSet().stream().skip(offset).limit(size).toList(), posts.stream().map(Post::getId).toList());
    }

    @Test
    void search_searchPosts() {
        var satisfyingPostIds = new TreeSet<Long>();
        var satisfyingPostId = insertPost("Заголовок 1", "Тело 1", 0, null, false);
        insertPostTag(satisfyingPostId, "tag1");
        insertPostTag(satisfyingPostId, "tag2");
        satisfyingPostIds.add(satisfyingPostId);

        satisfyingPostId = insertPost("Заголовок 11", "Тело 11", 0, null, false);
        insertPostTag(satisfyingPostId, "tag1");
        insertPostTag(satisfyingPostId, "tag2");
        insertPostTag(satisfyingPostId, "tag3");
        satisfyingPostIds.add(satisfyingPostId);

        var notSatisfyingPostId = insertPost("Заголовок 113", "Тело 2", 0, null, false);
        insertPostTag(notSatisfyingPostId, "tag1");

        notSatisfyingPostId = insertPost("Заголовок 2", "Тело 2", 0, null, false);
        insertPostTag(notSatisfyingPostId, "tag1");
        insertPostTag(notSatisfyingPostId, "tag2");

        notSatisfyingPostId = insertPost("Заголовок 3", "Тело 3", 0, null, false);
        insertPostTag(notSatisfyingPostId, "tag3");

        var posts = postRepository.findAll("заголовок 1", Set.of("tag1", "tag2"), 10, 0);
        var count = postRepository.count("заголовок 1", Set.of("tag1", "tag2"));
        assertEquals(satisfyingPostIds.size(), count);
        assertIterableEquals(satisfyingPostIds.descendingSet().stream().toList(), posts.stream().map(Post::getId).toList());
    }

    @Test
    void create_shouldPersistPost() {
        var post = postRepository.create("Заголовок", "Тело", Set.of("tag1", "tag2"));
        assertNotNull(post);
        assertAll(
                () -> assertNotNull(post.getId()),
                () -> assertEquals("Заголовок", post.getTitle()),
                () -> assertEquals("Тело", post.getText()),
                () -> assertEquals(0, post.getLikesCount()),
                () -> assertNull(post.getImageUuid()),
                () -> assertEquals(Set.of("tag2", "tag1"), post.getTags()),
                () -> assertFalse(post.isDeleted())
        );

        var dbPost = postRepository.findById(post.getId()).orElse(null);
        assertNotNull(dbPost);
        assertPostEquals(post, dbPost);
    }

    @Test
    void update_shouldUpdatePost() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");

        postRepository.update(postId, "Заголовок 2", "Тело 2", Set.of("tag3", "tag4"));
        var updatedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(updatedPost);

        assertAll(
                () -> assertEquals("Заголовок 2", updatedPost.getTitle()),
                () -> assertEquals("Тело 2", updatedPost.getText()),
                () -> assertEquals(4, updatedPost.getLikesCount()),
                () -> assertEquals(UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), updatedPost.getImageUuid()),
                () -> assertEquals(Set.of("tag3", "tag4"), updatedPost.getTags()),
                () -> assertFalse(updatedPost.isDeleted())
        );
    }

    @Test
    void update_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");
        var beforePost = postRepository.findById(postId).orElseThrow();

        postRepository.update(100500L, "Заголовок 2", "Тело 2", Set.of("tag3", "tag4"));
        assertTrue(postRepository.findById(100500L).isEmpty());

        var afterPost = postRepository.findById(postId).orElseThrow();

        assertPostEquals(beforePost, afterPost);
    }

    @Test
    void incrementLike_shouldIncreaseLikesCount() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);

        postRepository.incrementLikes(postId);

        var updatedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(updatedPost);

        assertEquals(5, updatedPost.getLikesCount());
    }

    @Test
    void incrementLike_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");
        var beforePost = postRepository.findById(postId).orElseThrow();

        postRepository.incrementLikes(100500L);
        assertTrue(postRepository.findById(100500L).isEmpty());

        var afterPost = postRepository.findById(postId).orElseThrow();

        assertPostEquals(beforePost, afterPost);
    }

    @Test
    void updateImageUuid_shouldUpdateImageUuid() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);

        var newImageUuid = UUID.fromString("90167857-9a33-4643-a2b6-3253c246099e");
        postRepository.updateImageUuid(postId, newImageUuid);

        var updatedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(updatedPost);

        assertEquals(newImageUuid, updatedPost.getImageUuid());
    }

    @Test
    void updateImageUuid_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");
        var beforePost = postRepository.findById(postId).orElseThrow();

        var newImageUuid = UUID.fromString("90167857-9a33-4643-a2b6-3253c246099e");
        postRepository.updateImageUuid(100500L, newImageUuid);
        assertTrue(postRepository.findById(100500L).isEmpty());

        var afterPost = postRepository.findById(postId).orElseThrow();

        assertPostEquals(beforePost, afterPost);
    }

    @Test
    void deleteById_shouldDeletePost() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);

        postRepository.deleteById(postId);

        var updatedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(updatedPost);

        assertTrue(updatedPost.isDeleted());
    }

    @Test
    void deleteById_shouldDoNothing() {
        var postId = insertPost("Заголовок 1", "Тело 1", 4, UUID.fromString("3829bee5-8701-4368-8dc2-9c07a782d089"), false);
        insertPostTag(postId, "tag1");
        insertPostTag(postId, "tag2");
        var beforePost = postRepository.findById(postId).orElseThrow();

        postRepository.deleteById(100500L);
        assertTrue(postRepository.findById(100500L).isEmpty());

        var afterPost = postRepository.findById(postId).orElseThrow();

        assertPostEquals(beforePost, afterPost);
    }

    private void assertPostEquals(Post expected, Post actual) {
        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertEquals(expected.getTitle(), actual.getTitle()),
                () -> assertEquals(expected.getText(), actual.getText()),
                () -> assertEquals(expected.getLikesCount(), actual.getLikesCount()),
                () -> assertEquals(expected.getImageUuid(), actual.getImageUuid()),
                () -> assertEquals(expected.getTags(), actual.getTags()),
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

    private void insertPostTag(long postId, String tag) {
        jdbcTemplate.update("""
                insert into post_tag (post_id, tag) values (?, ?);
                """, postId, tag);
    }
}
