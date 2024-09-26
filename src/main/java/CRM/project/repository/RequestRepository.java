package CRM.project.repository;

import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity,Integer> {
    Optional<RequestEntity> findByName(String fileName);

    List<RequestEntity> findByUnit(String unit);

    Optional<RequestEntity> findByStatus(String status);

    List<RequestEntity> findByStatusAndUnit(Status status, String department);

    List<RequestEntity> findByTechnician(String staffName);

    List<RequestEntity> findByCreatedTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    List<RequestEntity> findByRequesterAndStatus(String staffName, Status status);

    List<RequestEntity> findByRequester(String staffName);

    List<RequestEntity> findByStatusAndUnitAndTechnician(Status status, String departmentName, String technician);

    List<RequestEntity> findByRequesterUnitAndStatus(String departmentName, Status status);

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
}
