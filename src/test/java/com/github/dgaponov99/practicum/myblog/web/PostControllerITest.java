package com.github.dgaponov99.practicum.myblog.web;

import com.github.dgaponov99.practicum.myblog.PostgresRepositoryTest;
import com.github.dgaponov99.practicum.myblog.config.RepositoryITConfig;
import com.github.dgaponov99.practicum.myblog.config.ServiceITConfig;
import com.github.dgaponov99.practicum.myblog.config.WebITConfig;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(name = "persistence", classes = RepositoryITConfig.class),
        @ContextConfiguration(name = "service", classes = ServiceITConfig.class),
        @ContextConfiguration(name = "web", classes = WebITConfig.class)
})
@SpringJUnitConfig
public class PostControllerITest extends PostgresRepositoryTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate.update("truncate table posts, post_tag, comments restart identity cascade;");
    }

    @Test
    public void fullLifecycle_ok() throws Exception {
        var createPostJsonResponse = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Заголовок",
                                  "text": "Текст",
                                  "tags": ["tag1", "tag2"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        var postId = JsonPath.read(createPostJsonResponse, "$.id");
        mockMvc.perform(post("/api/posts/{id}/likes", postId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Новый заголовок",
                                  "text": "Новый текст",
                                  "tags": ["tag1", "tag3"]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Комментарий"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Комментарий"));

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Новый заголовок"))
                .andExpect(jsonPath("$.text").value("Новый текст"))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag1", "tag3")))
                .andExpect(jsonPath("$.likesCount").value(1))
                .andExpect(jsonPath("$.commentsCount").value(1));

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(0));
    }

}
