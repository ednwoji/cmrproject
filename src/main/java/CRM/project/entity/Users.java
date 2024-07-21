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
    @Column(name = "roles")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<String> roles;

    @Enumerated(EnumType.STRING)
    private Availability availability;
}
