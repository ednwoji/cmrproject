package CRM.project.entity;

import javax.persistence.*;

import CRM.project.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @ElementCollection
    @Column(name = "user_roles")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name = "helpdesk_users_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private List<String> userRoles;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    @Column(name = "created_by")
    private String createdBy;
}
