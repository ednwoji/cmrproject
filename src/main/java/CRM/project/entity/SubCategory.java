package CRM.project.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "request_sub_category")
@EntityListeners(AuditingEntityListener.class)
public class SubCategory extends TimeClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String subCategoryName;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    private int sla;

    @ManyToOne
    @JoinColumn(name = "subCategory_id",referencedColumnName = "categoryId")
    private Category category;
}
