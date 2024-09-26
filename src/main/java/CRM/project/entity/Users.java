package CRM.project.entity;

import javax.persistence.*;

import CRM.project.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "helpdesk_users")
@EntityListeners(AuditingEntityListener.class)
public class Users extends TimeClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department unitName;
    private String staffName;
    private String userEmail;

    @Transient
    private List<String> userRoles;

    @Enumerated(EnumType.STRING)
    private UserStatus status;


    @Enumerated(EnumType.STRING)
    private Availability availability;
}
