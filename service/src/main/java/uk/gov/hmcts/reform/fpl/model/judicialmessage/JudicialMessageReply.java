package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class JudicialMessageReply {
    private final String dateSent;
    private final LocalDateTime updatedTime;
    private final String message;
    private final String replyFrom;
    private final String replyTo;
}
