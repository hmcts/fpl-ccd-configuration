package uk.gov.hmcts.reform.fpl.model.children;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason;

@Value
@Builder
@Jacksonized
public class ChildFinalDecisionDetails {
    String childNameLabel;
    ChildFinalDecisionReason finalDecisionReason;
}
