package com.github.dgaponov99.practicum.myblog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.github.dgaponov99.practicum.myblog.service", "com.github.dgaponov99.practicum.myblog.mapper"})
public class ServiceITConfig {
}
