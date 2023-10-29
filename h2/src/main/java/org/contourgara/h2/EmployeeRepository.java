package org.contourgara.h2;

import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(String id);

    Integer insert(Employee employee);
}
