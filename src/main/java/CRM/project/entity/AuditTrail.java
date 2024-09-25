package CRM.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AuditTrail {

    @Column(name = "audit_user_name")
    private String user;

    @Column(name = "user_action")
    private String action;

    @Column(name = "comment_time")
    private LocalDateTime auditTime;
}
