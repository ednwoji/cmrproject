package CRM.project.entity;

import javax.persistence.*;

import CRM.project.dto.CommentData;
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

@Entity
@Data
@Table(name = "helpdesk_requests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RequestEntity extends TimeClass {
//   @Id
//   @GeneratedValue(strategy = GenerationType.AUTO)
//   @Column(name = "helpdesk_request_id")
//   private int id;

    @Id
    @GeneratedValue(generator = "custom-uuid-generator")
    @GenericGenerator(name = "custom-uuid-generator", strategy = "CRM.project.entity.CustomIdGenerator")
    @Column(name = "helpdesk_request_id", updatable = false, nullable = false)
    private String requestId;
   private String unit;
   private LocalDateTime logTime;
   private String subject;
   private String priority;
   private String category;
   private String subCategory;

   @Lob
   @Column(length = 10485760)
   private String description;
   private String technician;

    @ElementCollection
    @Column(name = "attachment_data")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name = "request_entity_attachment_data", joinColumns = @JoinColumn(name = "helpdesk_request_id"))
    private List<FileMetaData> files;

   private String email;
   @Enumerated(EnumType.STRING)
   private Status status;

    private LocalDateTime dueDate;
   private int sla;
   private String requester;
   private String requesterUserName;
   private String requesterUnit;

    @ElementCollection
    @Column(name = "comment_data")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name = "request_entity_comment_data", joinColumns = @JoinColumn(name = "helpdesk_request_id"))
    private List<CommentData> commentData;

    private LocalDateTime resolutionTime;
    private String resolutionTechnician;
    private boolean resolvedWithinSla = false;

    private String closureComments;
    private LocalDateTime closureTime;
    private int rating;

    @ElementCollection
    @Column(name = "audit_trail")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name = "request_entity_audit_trail", joinColumns = @JoinColumn(name = "helpdesk_request_id"))
    private List<AuditTrail> auditTrails;
}
