package com.github.dgaponov99.practicum.myblog.repository;

import com.github.dgaponov99.practicum.myblog.configuration.DataSourceConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.github.dgaponov99.practicum.myblog.persistence")
@Import(DataSourceConfiguration.class)
public class RepositoryTestConfig {
}
