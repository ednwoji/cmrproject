package CRM.project.repository;

import CRM.project.entity.Solutions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SolutionRepository extends JpaRepository<Solutions, Long> {
}
