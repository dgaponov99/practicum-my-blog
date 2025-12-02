package com.github.dgaponov99.practicum.myblog.config;

import com.github.dgaponov99.practicum.myblog.configuration.ValidationConfiguration;
import com.github.dgaponov99.practicum.myblog.configuration.WebConfiguration;
import com.github.dgaponov99.practicum.myblog.service.CommentService;
import com.github.dgaponov99.practicum.myblog.service.PostService;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan("com.github.dgaponov99.practicum.myblog.web")
@Import({ValidationConfiguration.class, WebConfiguration.class})
public class WebTestConfig {

    @Bean
    @Primary
    public PostService postService() {
        return Mockito.mock(PostService.class);
    }

    @Bean
    @Primary
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }

}
