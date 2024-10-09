package CRM.project.repository;

import CRM.project.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Long> {
 Optional<Department>findByDepartmentName (String departmentName);
 List<Department> findAllByDepartmentName(String departmentName);

 Optional<Department> findAllBydepartmentId(Long aLong);
}
