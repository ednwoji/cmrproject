package CRM.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto {

    private String body;
    private String footer;
    private String subject;

    @JsonProperty("recipients")
    private List<Recipient> recipients;

    @JsonProperty("sender")
    private Sender sender;

    @JsonProperty("attachments")
    private List<Attachment> attachments;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recipient {
        private String displayName;
        private String recipientType; // e.g., "TO", "CC", "BCC"
        private String email;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sender {
        private String displayName;
        private String email;
    }

    // Inner class for Attachment
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String filename;
        private String contentType;
        private byte[] data;
        private boolean inline;
        private String name;
        private String mime;
        private String content;
    }
}


