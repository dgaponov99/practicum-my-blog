package com.github.dgaponov99.practicum.myblog.repository;

import com.github.dgaponov99.practicum.myblog.configuration.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestcontainersConfiguration.class)
public abstract class RepositoryBaseTest {
}
