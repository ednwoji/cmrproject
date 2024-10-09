package CRM.project.service;

import CRM.project.entity.Department;
import CRM.project.entity.Users;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImp implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Override
    public Department addNewDepartment(Department department) {

        Department department1=departmentRepository.findByDepartmentName(department.getDepartmentName()).orElse(null);
        return department1== null ? departmentRepository.save(department) : null;
    }

    @Override
    public List<Department> findAllDepartmentsByName(String departmentName) {
        return departmentRepository.findAllByDepartmentName(departmentName);
    }

    @Override
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department findDepartmentById(String deptId) {
        return departmentRepository.findAllBydepartmentId(Long.valueOf(deptId)).orElse(null);
    }


}
