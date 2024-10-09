package CRM.project.service;

import CRM.project.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    Department addNewDepartment(Department department);

    List<Department> findAllDepartmentsByName(String departmentName);

    List<Department> getDepartments();


    Department findDepartmentById(String deptId);
}
