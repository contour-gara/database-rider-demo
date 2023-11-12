package org.contourgara.databaserider;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DBRider
@DBUnit(caseInsensitiveStrategy = LOWERCASE, cacheConnection = false)
class EmployeeRepositoryImplTest {
    // Test Container 起動
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE).withTag("11.9"))
                    .withUsername("user")
                    .withPassword("p@ssw0rd")
                    .withDatabaseName("demo");

    // Database Rider にデータベースの接続先を連携
    private static final ConnectionHolder connectionHolder =
            () -> DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );

    @Autowired
    EmployeeRepositoryImpl sut;

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
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DataSet(value = "datasets/setup/findbyid.xml")
        void XMLファイルでデータセット() {
            // execute
            Optional<Employee> actual = sut.findById("1");

            // assert
            Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DataSet(value = "datasets/setup/findbyid.json")
        void JSONファイルでデータセット() {
            // execute
            Optional<Employee> actual = sut.findById("1");

            // assert
            Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DataSet(value = "datasets/setup/employees.csv")
        void CSVファイルでデータセット() {
            // execute
            Optional<Employee> actual = sut.findById("1");

            // assert
            Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DataSet(cleanBefore = true, executeScriptsBefore = "datasets/setup/findbyid.sql")
        void SQLファイルでデータセット() {
            // execute
            Optional<Employee> actual = sut.findById("1");

            // assert
            Optional<Employee> expected = Optional.of(new Employee("1", "Taro", "Yamada"));
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DataSet(value = {
                "datasets/setup/findbyid.yml",
                "datasets/setup/findbyid2.yml"
        })
        void 複数ファイルでデータセット() {
            // execute
            Optional<Employee> actual1 = sut.findById("1");
            Optional<Employee> actual2 = sut.findById("2");

            // assert
            Optional<Employee> expected1 = Optional.of(new Employee("1", "Taro", "Yamada"));
            assertThat(actual1).isEqualTo(expected1);

            Optional<Employee> expected2 = Optional.of(new Employee("2", "Jiro", "Yamada"));
            assertThat(actual2).isEqualTo(expected2);
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
        @DataSet(cleanBefore = true, value = "datasets/setup/2table.yml")
        @ExpectedDataSet(value = "datasets/expected/1table.yml")
        void 期待値の方がテーブルが少なくても成功() {
        }

        @Test
        @DataSet(cleanBefore = true, value = "datasets/setup/1table.yml")
        @ExpectedDataSet(value = "datasets/expected/2table.yml")
        void 期待値の方がテーブルが多いと失敗() {
        }
    }

    @Nested
    class レコードの過不足によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(value = "datasets/expected/1record.yml")
        void 期待値の方がレコードが少ないと失敗() {
        }

        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/2record.yml")
        void 期待値のほうがレコードが多いと失敗() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/1record.yml",
                compareOperation = CONTAINS
        )
        void 期待値が含まれるかどうかをテストすれば成功() {
        }
    }

    @Nested
    class カラムの過不足によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/undercolumn.yml")
        void 期待値のカラムが少ないと成功() {
        }

        @Test
        @DataSet(value = "datasets/setup/1record.yml")
        @ExpectedDataSet(value = "datasets/expected/overcolumn.yml")
        void 期待値のカラムが多いと失敗() {
        }
    }

    @Nested
    class レコードの順序によるアサーションの挙動 {
        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(value = "datasets/expected/2record-reverse.yml")
        void レコードの順序が違うと失敗() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/2record-reverse.yml",
                compareOperation = CONTAINS
        )
        void 期待値が含まれるかどうかをテストすれば成功() {
        }

        @Test
        @DataSet(value = "datasets/setup/2record.yml")
        @ExpectedDataSet(
                value = "datasets/expected/2record-reverse.yml",
                orderBy = "id"
        )
        void レコードをソートすると成功() {
        }
    }
}
