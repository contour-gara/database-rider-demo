package org.contourgara.h2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class H2ApplicationTest {
    @Autowired
    EmployeeRepositoryImpl employeeRepositoryimple;

    @Test
    void contextLoads() {
        assertThat(employeeRepositoryimple).isNotNull();
    }
}
