package org.contourgara.h2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Employee> findById(String id) {
        try {
            Employee employee = jdbcTemplate.queryForObject("""
                            SELECT
                              id,
                              first_name AS firstName,
                              last_name AS lastName
                            FROM
                              employees
                            WHERE
                              id = :id
                            """,
                    new MapSqlParameterSource()
                            .addValue("id", id),
                    new DataClassRowMapper<>(Employee.class)
            );
            return Optional.ofNullable(employee);
        } catch (EmptyResultDataAccessException e) {
            log.warn("idに該当する従業員情報がありませんでした。", e);
            return Optional.empty();
        }
    }

    @Override
    public Integer insert(Employee employee) {
        return jdbcTemplate.update("""
                        INSERT INTO employees (id, first_name, last_name)
                        VALUES (:id, :firstName, :lastName);
                        """,
                new MapSqlParameterSource()
                        .addValue("id", employee.id())
                        .addValue("firstName", employee.firstName())
                        .addValue("lastName", employee.lastName())
        );
    }
}
