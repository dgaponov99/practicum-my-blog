package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
        }
)
public abstract class ServiceBaseTest {

    @MockitoBean
    protected PostRepository postRepository;
    @MockitoBean
    protected CommentRepository commentRepository;
    @MockitoBean
    protected PostImageService postImageService;

}
