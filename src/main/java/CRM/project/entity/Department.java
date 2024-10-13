package CRM.project.entity;

import  javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_department")
public class Department extends TimeClass {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long departmentId;
   private String departmentName;
   private String email;
   private boolean autoAssign = true;
   @Transient
   private String creationType;
}
