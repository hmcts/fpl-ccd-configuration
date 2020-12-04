package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;

import java.time.LocalDateTime;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@ToString(callSuper = true)
public class JudicialMessage extends JudicialMessageMetaData {
    private final String dateSent;
    private final LocalDateTime dateSentAsLocalDateTime;
    private final String note;
    private final JudicialMessageStatus status;
    private final String relatedDocuments;
}
