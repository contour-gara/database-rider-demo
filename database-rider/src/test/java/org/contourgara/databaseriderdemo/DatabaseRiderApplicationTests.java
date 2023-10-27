package org.contourgara.databaseriderdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableAutoConfiguration(exclude = {JdbcRepositoriesAutoConfiguration.class})
class DatabaseRiderApplicationTests {
    @Autowired
    EmployeeRepositoryimple employeeRepositoryimple;

    @Test
    void contextLoads() {
        assertThat(employeeRepositoryimple).isNotNull();
    }
}
