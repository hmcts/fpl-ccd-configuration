package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UploadedCaseManagementOrder {
    private UUID id;
    private DocumentReference caseManagementOrder;
    private LocalDate dateSent; // hearing date
    private CMOStatus status;
}
