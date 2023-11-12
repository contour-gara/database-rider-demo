package org.contourgara.h2;

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

import java.sql.DriverManager;
import java.util.Optional;

@SpringBootTest
@DBRider
@DBUnit(cacheConnection = false)
class EmployeeRepositoryImplTest {
    private static final String DB_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "sa";

    private static final ConnectionHolder connectionHolder =
            () -> DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

    @Autowired
    EmployeeRepositoryImpl sut;

    @BeforeAll
    static void setUpAll() {
        Flyway.configure().dataSource(DB_URL, DB_USER, DB_PASSWORD).load().migrate();
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
}
