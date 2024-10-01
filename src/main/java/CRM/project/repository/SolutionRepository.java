package CRM.project.repository;

import CRM.project.entity.Solutions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SolutionRepository extends JpaRepository<Solutions, Long> {
    Optional<Solutions> findBySolutionId(Long aLong);

    void deleteBySolutionId(Long solutionId);
}
