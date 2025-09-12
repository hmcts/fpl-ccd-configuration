package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@ToString(callSuper = true)
public class JudicialMessageReply {
    private final String dateSent;
    private final LocalDateTime updatedTime;
    private final String message;
    private final String replyFrom;
    private final String replyTo;
    private final String urgency;
}
