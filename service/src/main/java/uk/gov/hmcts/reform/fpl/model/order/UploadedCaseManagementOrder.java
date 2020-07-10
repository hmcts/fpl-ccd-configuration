package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

@Data
public class UploadedCaseManagementOrder {
    private DocumentReference caseManagementOrder;
    private LocalDate dateSent; // hearing date
    private CMOStatus status;
}
