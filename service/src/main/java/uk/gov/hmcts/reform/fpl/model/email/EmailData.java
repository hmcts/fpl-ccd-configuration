package uk.gov.hmcts.reform.fpl.model.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Value
@Builder(toBuilder = true)
public class EmailData {
    private final String recipient;
    private final String subject;
    private final String message;
    private final Set<EmailAttachment> attachments;

    @JsonIgnore
    public boolean hasAttachments() {
        return (!isEmpty(attachments));
    }
}
