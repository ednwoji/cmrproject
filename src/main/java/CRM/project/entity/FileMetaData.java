package CRM.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class FileMetaData {

        private Long id;
        private String name;
        private String type;
        private String filePath;
}
