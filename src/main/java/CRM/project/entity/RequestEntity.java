package CRM.project.entity;

import javax.persistence.*;

import CRM.project.dto.CommentData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "helpdesk_request_id")
   private int id;
   private String unit;
   private LocalDateTime logTime;
   private String subject;
   private String priority;
   private String category;
   private String subCategory;
   private String description;
   private String technician;
   private String name;
   private String type;
   private String email;
   @Enumerated(EnumType.STRING)
   private Status status;
   private String filePath;
   private LocalDateTime dueDate;
   private int sla;
   private String requester;
   private String requesterUnit;

    @ElementCollection
    @Column(name = "comment_data")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name = "request_entity_comment_data", joinColumns = @JoinColumn(name = "helpdesk_request_id"))
    private List<CommentData> commentData;

    private String closureComments;
    private LocalDateTime closureTime;
    private int rating;
}
