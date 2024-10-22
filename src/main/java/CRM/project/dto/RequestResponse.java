package CRM.project.dto;


import javax.persistence.*;

import CRM.project.dto.CommentData;
import CRM.project.entity.AuditTrail;
import CRM.project.entity.FileMetaData;
import CRM.project.entity.Status;
import CRM.project.entity.TimeClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestResponse {

    private String requestId;
    private String unit;
    private LocalDateTime logTime;
    private String subject;
    private String priority;
    private String category;
    private String subCategory;
    private String technician;
    private Status status;
    private LocalDateTime dueDate;
    private String requester;
    private String resolutionTechnician;
    private boolean resolvedWithinSla;
}

