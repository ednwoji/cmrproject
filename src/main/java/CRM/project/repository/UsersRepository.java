package CRM.project.repository;

import CRM.project.dto.Availability;
import CRM.project.entity.Department;
import CRM.project.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users,Long> {
    Optional<Users> findByStaffName (String staffName);

    List<Users> findAllByUnitName(Department unit);

    List<Users> findAllByUnitNameAndAvailability(Department unit, Availability availability);

    List<Users> findByUnitName(Department unit);

    Optional<Users> findByUserEmail(String s);

    Optional<Users> findByUserId(Long userId);
}
