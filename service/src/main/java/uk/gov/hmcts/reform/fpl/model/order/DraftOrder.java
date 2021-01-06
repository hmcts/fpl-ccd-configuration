package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class DraftOrder {
    private UUID hearingId;
    private String title;
    private DocumentReference document;
    private CMOStatus status;
    private LocalDate dateSent;
    private String hearing;
}
