package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder
public class ReviewCaseManagementOrder {
    private final CMOStatus cmoStatus;
    private final DocumentReference orderDoc;
}
