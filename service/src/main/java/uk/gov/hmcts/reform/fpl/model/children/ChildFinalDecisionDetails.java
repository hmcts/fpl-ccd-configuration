package uk.gov.hmcts.reform.fpl.model.children;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ChildFinalDecisionDetails {
    String childNameLabel;
    String finalDecisionReason;
}
