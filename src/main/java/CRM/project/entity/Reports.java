package CRM.project.entity;

import CRM.project.dto.ReportData;
import CRM.project.utils.ReportDataConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "user_reports", uniqueConstraints = @UniqueConstraint(columnNames = {"report_name", "created_by"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reports {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long reportId;

    @Column(name = "report_name")
    private String reportName;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "created_by")
    private String createdBy;

    private LocalDateTime createdTime;

    @Transient
    private List<String> requiredData;

    @Convert(converter = ReportDataConverter.class)
//    @Column(name = "report_data", columnDefinition = "TEXT")
    @Column(name = "report_data", columnDefinition = "CLOB")
    private ReportData reportData;

    @Column(name = "report_query")
    @JsonIgnore
    @Lob
    private String reportQuery;
}

