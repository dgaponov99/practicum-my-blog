package com.github.dgaponov99.practicum.myblog;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.github.dgaponov99.practicum.myblog")
@PropertySource("classpath:application.properties")
public class WebConfiguration {
}
