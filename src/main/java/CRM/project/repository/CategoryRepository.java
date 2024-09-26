package CRM.project.repository;

import CRM.project.entity.Category;
import CRM.project.entity.Department;
import CRM.project.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByCategoryName (String categoryName);

    List<Category> findByUnitName(Department department1);

    Optional<Category> findByCategoryId(Long aLong);

    void deleteByCategoryId(long categoryId);
}
