package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.time.LocalDateTime;
import javax.validation.constraints.PastOrPresent;

@Data
@Builder
public class SupportingEvidenceBundle {
    private final String name;
    private final String notes;
    @PastOrPresent(message = "Date of time received cannot be in the future", groups = DateOfIssueGroup.class)
    private final LocalDateTime dateTimeReceived;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
}
