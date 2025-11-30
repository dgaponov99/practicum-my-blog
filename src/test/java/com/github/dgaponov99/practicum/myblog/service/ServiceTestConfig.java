package com.github.dgaponov99.practicum.myblog.service;

import com.github.dgaponov99.practicum.myblog.persistence.repository.CommentRepository;
import com.github.dgaponov99.practicum.myblog.persistence.repository.PostRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan({"com.github.dgaponov99.practicum.myblog.service", "com.github.dgaponov99.practicum.myblog.mapper"})
public class ServiceTestConfig {

    @Bean
    @Primary
    public PostRepository postRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    @Primary
    public CommentRepository commentRepository() {
        return Mockito.mock(CommentRepository.class);
    }

    @Bean
    @Primary
    public PostImageService postImageService() {
        return Mockito.mock(PostImageService.class);
    }


}
