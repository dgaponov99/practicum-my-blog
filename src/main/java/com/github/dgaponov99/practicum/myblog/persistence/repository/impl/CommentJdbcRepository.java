package com.github.dgaponov99.practicum.myblog.persistence.repository.impl;

import com.github.dgaponov99.practicum.myblog.persistence.entity.Comment;
import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentJdbcRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Optional<Comment> findById(Long id) {
        return jdbcTemplate.query("select comment_id, post_id, text, deleted from comments where comment_id = ?;",
                (rs, rowNum) -> mapRow(rs), id).stream().findFirst();
    }

    @Override
    public int countByPostId(Long postId) {
        return jdbcTemplate.queryForObject("select count(*) count from comments where not deleted and post_id = ?;", Integer.class, postId);
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jdbcTemplate.query("select comment_id, post_id, text, deleted from comments where not deleted and post_id = ?;",
                (rs, rowNum) -> mapRow(rs), postId);
    }

    @Override
    public Comment create(Long postId, String text) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("insert into comments (post_id, text) values (?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, postId);
            statement.setString(2, text);
            return statement;
        }, keyHolder);
        var commentId = (long) keyHolder.getKeys().get("comment_id");
        return findById(commentId).get();
    }

    @Override
    public void update(Long id, String text) {
        jdbcTemplate.update("update comments set text = ? where post_id = ?;", text, id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("update comments set deleted = true where comment_id = ?;", id);
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getLong("comment_id"),
                rs.getLong("post_id"),
                rs.getString("text"),
                rs.getBoolean("deleted")
        );
    }
}
