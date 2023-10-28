package org.contourgara.databaserider;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
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
import static com.github.database.rider.core.api.dataset.CompareOperation.CONTAINS;


@SpringBootTest
@Testcontainers
@DBRider
@DBUnit(caseInsensitiveStrategy = LOWERCASE, cacheConnection = false)
class EmployeeRepositoryimpleTest {
    // Test Container 起動
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag("11.9"))
                    .withUsername("user")
                    .withPassword("password")
                    .withDatabaseName("sample");

    // セータベース接続の定義
    private static final ConnectionHolder connectionHolder =
            () -> DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );

    @Autowired
    EmployeeRepositoryimple sut;

    // Application.yml の値を Test Container の情報で上書き
    @DynamicPropertySource
    static void setUpProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgres::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgres::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgres::getPassword);
    }

    // データベースのマイグレーション
    @BeforeAll
    static void setUpAll() {
        Flyway.configure().dataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        ).load().migrate();
    }

    @Nested
    class データセット方法 {
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
    }

    @Nested
    class アサーション方法 {
        @Test
        @DataSet(cleanBefore = true)
        @ExpectedDataSet(value = "datasets/expected/insert.yml")
        void YAMLファイルでアサーション() {
            // setup
            Employee employee = new Employee("1", "Taro", "Yamada");
            // execute
            sut.insert(employee);
        }
    }

    @Nested
    class テーブルの過不足によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/2table.yml")
        @ExpectedDataSet(value = "datasets/expected/1table.yml")
        void 期待値の方がテーブルが少ない() {
        }

        @Test
        @DataSet(value = "datasets/setup/1table.yml")
        @ExpectedDataSet(value = "datasets/expected/2table.yml")
        void 期待値の方がテーブルが多い() {
        }
    }

    @Nested
    class レコードの過不足によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(value = "datasets/expected/1record.yml")
        void 期待値の方がレコードが少ない() {
        }

        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/2record.yml")
        void 期待値のほうがレコードが多い() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/1record.yml",
                compareOperation = CONTAINS
        )
        void 期待値が含まれるかどうかだけをアサーション() {
        }
    }

    @Nested
    class カラムの過不足によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/undercolumn.yml")
        void 期待値のカラムが少ない() {
        }

        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/overcolumn.yml")
        void 期待値のカラムが多い() {
        }
    }

    @Nested
    class レコードの順序によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(value = "datasets/expected/2record-reverse.yml")
        void レコードの順序が違う() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/2record-reverse.yml",
                compareOperation = CONTAINS
        )
        void 含まれるかどうかをテストする() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/2record-reverse.yml",
                orderBy = "id"
        )
        void レコードをソートする() {
        }
    }
}
