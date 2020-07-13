package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;

@Data
@Builder
public class UploadedCaseManagementOrder {
    private DocumentReference caseManagementOrder;
    private String hearing;
    private LocalDate dateSent;
    private CMOStatus status;
}
