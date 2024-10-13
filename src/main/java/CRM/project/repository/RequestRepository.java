package CRM.project.repository;

import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity,Integer> {

    List<RequestEntity> findByUnit(String unit);

    Page<RequestEntity> findByStatus(Status status, Pageable pageable);

    List<RequestEntity> findByStatusAndUnit(Status status, String department);

    List<RequestEntity> findByTechnician(String staffName);

    List<RequestEntity> findByCreatedTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    Page<RequestEntity> findByRequesterAndStatus(String staffName, Status status, Pageable pageable);

    Page<RequestEntity> findByRequester(String staffName, Pageable pageable);

    Page<RequestEntity> findByStatusAndUnitAndTechnician(Status status, String departmentName, String technician, Pageable pageable);

    Page<RequestEntity> findByRequesterUnitAndStatus(String departmentName, Status status, Pageable pageable);

    List<RequestEntity> findByUnitAndCreatedTimeBetween(String unitName, LocalDateTime localDateTime, LocalDateTime localDateTime1);


    @Query("SELECT MONTH(r.logTime) AS month, COUNT(r) AS count " +
            "FROM RequestEntity r " +
            "WHERE r.logTime IS NOT NULL " +
            "GROUP BY MONTH(r.logTime), YEAR(r.logTime)")
    List<Object[]> findAdminRequestsGroupedByMonth();

    @Query("SELECT MONTH(r.logTime) AS month, COUNT(r) AS count " +
            "FROM RequestEntity r " +
            "WHERE r.logTime IS NOT NULL " +
            "AND r.technician =:username " +
            "GROUP BY MONTH(r.logTime), YEAR(r.logTime)")
    List<Object[]> findRequestsGroupedByMonth(@Param("username") String username);

    Optional<RequestEntity> findByRequestId(String requestId);

    Page<RequestEntity> findByStatusNotInAndUnitAndTechnician(List<Status> excludedStatuses, String departmentName, String technician, Pageable pageable);

    Page<RequestEntity> findByRequesterAndStatusNotIn(String staffName, List<Status> excludedStatuses, Pageable pageable);

    Page<RequestEntity> findByRequesterUnitAndStatusNotIn(String departmentName, List<Status> excludedStatuses, Pageable pageable);

    Page<RequestEntity> findByStatusNotIn(List<Status> excludedStatuses, Pageable pageable);
}
