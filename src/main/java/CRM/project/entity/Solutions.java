package CRM.project.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_solutions")
public class Solutions {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long solutionId;
    private String department;
    private String categoryName;
    private String subCategoryName;
    private String description;
    private String fileType;
    private String fileName;
    private String filePath;
    private String createdBy;

    @Transient
    private byte[] file;

}
