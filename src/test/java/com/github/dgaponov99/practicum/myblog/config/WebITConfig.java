package com.github.dgaponov99.practicum.myblog.config;

import com.github.dgaponov99.practicum.myblog.configuration.ValidationConfiguration;
import com.github.dgaponov99.practicum.myblog.configuration.WebConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.github.dgaponov99.practicum.myblog.web")
@Import({ValidationConfiguration.class, WebConfiguration.class})
public class WebITConfig {
}
