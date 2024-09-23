package CRM.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class CommentData {

    @Column(name = "comment_user_name")
    private String user;

    @Column(name = "user_comment")
    private String comment;

    @Column(name = "comment_time")
    private LocalDateTime commentTime;
}
