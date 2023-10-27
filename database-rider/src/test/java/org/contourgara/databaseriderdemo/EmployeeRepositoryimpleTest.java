package org.contourgara.databaseriderdemo;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;
import java.util.Optional;

import static com.github.database.rider.core.api.configuration.Orthography.LOWERCASE;

@SpringBootTest
@Testcontainers
@DBRider
@DBUnit(caseInsensitiveStrategy = LOWERCASE, cacheConnection = false)
class EmployeeRepositoryimpleTest {
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag("11.9"))
                    .withUsername("user")
                    .withPassword("password")
                    .withDatabaseName("sample");

    private static final ConnectionHolder connectionHolder =
            () -> DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );

    @Autowired
    EmployeeRepositoryimple sut;

    @DynamicPropertySource
    static void setUpProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgres::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgres::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void setUpAll() {
        Flyway.configure().dataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        ).load().migrate();
    }

    @Test
    @DataSet(value = "datasets/setup/findbyid.yml")
    void YAMLファイルでデータセット() {
        // execute
        Optional<Employee> actual = sut.findById("1");
        // assert
        Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DataSet(cleanBefore = true, executeScriptsBefore = "datasets/setup/findbyid.sql")
    void SQLファイルでデータセット() {
        // execute
        Optional<Employee> actual = sut.findById("1");
        // assert
        Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DataSet(cleanBefore = true)
    @ExpectedDataSet(value = "datasets/expected/insert.yml")
    void データをセットしない場合は初期化() {
        // setup
        Employee employee = new Employee("1", "Taro", "Yamada");
        // execute
        sut.insert(employee);
    }
}
