package com.github.dgaponov99.practicum.myblog.persistence.repository.impl;

import com.github.dgaponov99.practicum.myblog.persistence.entity.Post;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostJdbcRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Post> findAll(String title, Set<String> tags, int size, int offset) {
        var headSql = """
                select p.post_id,
                       p.title,
                       p.text,
                       p.likes_count,
                       p.image_uuid,
                       p.deleted
                from posts p
                """;
        var params = new ArrayList<>();
        var sql = searchSql(headSql, title, tags, params);
        params.add(offset);
        params.add(size);
        sql = "%s order by post_id desc offset ? rows fetch next ? rows only;".formatted(sql);
        log.debug(sql);
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPost(rs), params.toArray());
    }

    @Override
    public int count(String title, Set<String> tags) {
        var headSql = """
                select count(*)
                from (select 1
                    from posts p
                """;
        var params = new ArrayList<>();
        var sql = "%s) as posts;".formatted(searchSql(headSql, title, tags, params));
        log.debug(sql);
        return jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jdbcTemplate.query(
                "select post_id, title, text, likes_count, image_uuid, deleted from posts where post_id = ?;",
                (rs, rowNum) -> mapPost(rs), id).stream().findFirst();
    }

    @Override
    public Post create(String title, String text, Set<String> tags) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "insert into posts (title, text) values (?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, text);
            return statement;
        }, keyHolder);
        var postId = (long) keyHolder.getKeys().get("post_id");
        new HashSet<>(tags).forEach(tag -> jdbcTemplate.update(
                "insert into post_tag (post_id, tag) values (?, ?);", postId, tag));
        return findById(postId).get();
    }

    @Override
    public void update(Long id, String title, String text, Set<String> tags) {
        if (Boolean.TRUE.equals(jdbcTemplate.queryForObject("select exists(select 1 from posts where post_id = ?);", Boolean.class, id))) {
            jdbcTemplate.update("update posts set title = ?, text = ? where post_id = ?;", title, text, id);
            jdbcTemplate.update("delete from post_tag where post_id = ?;", id);
            tags.forEach(tag -> jdbcTemplate.update("insert into post_tag (post_id, tag) values (?, ?);", id, tag));
        }
    }

    @Override
    public void updateImageUuid(Long id, UUID imageUuid) {
        jdbcTemplate.update("update posts set image_uuid = ? where post_id = ?;", imageUuid.toString(), id);
    }

    @Override
    public void incrementLikes(Long id) {
        jdbcTemplate.update("update posts set likes_count = likes_count + 1 where post_id = ?;", id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("update posts set deleted = true where post_id = ?;", id);
    }

    private Post mapPost(ResultSet rs) throws SQLException {
        var rowPost = new Post();
        rowPost.setId(rs.getLong("post_id"));
        rowPost.setTitle(rs.getString("title"));
        rowPost.setText(rs.getString("text"));
        rowPost.setLikesCount(rs.getInt("likes_count"));
        var imageUuid = rs.getString("image_uuid");
        if (imageUuid != null) {
            rowPost.setImageUuid(UUID.fromString(imageUuid));
        }
        rowPost.setTags(new HashSet<>(jdbcTemplate.query("select tag from post_tag where post_id = ?;",
                (tagRs, rowNum) -> tagRs.getString("tag"),
                rowPost.getId())));
        rowPost.setDeleted(rs.getBoolean("deleted"));
        return rowPost;
    }

    private String searchSql(String headSql, String title, Set<String> tags, List<Object> params) {
        var sql = new StringBuilder(headSql);

        var tagCount = tags == null ? 0 : tags.size();
        if (tagCount > 0) {
            sql.append(" left join post_tag pt on pt.post_id = p.post_id ");
        }

        sql.append(" where not p.deleted ");

        if (StringUtils.hasText(title)) {
            params.add("%%%s%%".formatted(title));
            sql.append(" and lower(p.title) like lower(?) ");
        }

        if (tagCount > 0) {
            var tagsQuery = tags.stream().peek(params::add).map(tag -> "?").collect(Collectors.joining(", "));
            sql.append(" and (pt.tag in (").append(tagsQuery).append(")) ");
            params.add(tagCount);
            sql.append("""
                     group by p.post_id, p.title, p.text, p.likes_count, p.image_uuid, p.deleted
                     having (count(distinct pt.tag) = ?)
                    """);
        }
        return sql.toString();
    }
}
