package uk.gov.hmcts.reform.fpl.model.children;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildFinalDecisionReason;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.ChildFinalDecision;

@Value
@Builder
@Jacksonized
public class ChildFinalDecisionDetails {
    @CCD(label = " ")
    String childNameLabel;
    @CCD(label = " ", typeParameterOverride = "ChildFinalDecision", typeParameterClass = ChildFinalDecision.class)
    ChildFinalDecisionReason finalDecisionReason;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Select final outcome for:**", typeOverride = FieldType.Label)
  private String childFinalDecisionDetailsSubHeadingLabel;
  // ==== end synthesised definition-only fields ====
}
