package com.github.dgaponov99.practicum.myblog;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.github.dgaponov99.practicum.myblog")
@PropertySource("classpath:application.properties")
public class AppConfiguration {
}
