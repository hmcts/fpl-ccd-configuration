package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class JudicialMessageReply {
    @CCD(label = "Date sent")
    private final String dateSent;
    @CCD(label = " ", showCondition = "dateSent=\"DO_NOT_SHOW\"")
    private final LocalDateTime updatedTime;
    @CCD(label = "Message")
    private final String message;
    @CCD(label = "Sender's email address")
    private final String replyFrom;
    @CCD(label = "Recipient's email address")
    private final String replyTo;
    @CCD(label = "Urgency")
    private final String urgency;
}
