package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
public class FactorsParenting {
    @CCD(label = "Anything else", typeOverride = FieldType.YesOrNo)
    private final String anythingElse;
    @CCD(label = "Alcohol or drug abuse", typeOverride = FieldType.YesOrNo)
    private final String alcoholDrugAbuse;
    @CCD(label = "Domestic violence", typeOverride = FieldType.YesOrNo)
    private final String domesticViolence;
    @CCD(label = "Give details", showCondition = "alcoholDrugAbuse=\"Yes\"", typeOverride = FieldType.TextArea)
    private final String alcoholDrugAbuseReason;
    @CCD(label = "Give details", showCondition = "domesticViolence=\"Yes\"", typeOverride = FieldType.TextArea)
    private final String domesticViolenceReason;
    @CCD(label = "Give details", showCondition = "anythingElse=\"Yes\"", typeOverride = FieldType.TextArea)
    private final String anythingElseReason;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Is there any evidence of any of the following affecting ability to parent?",
          typeOverride = FieldType.Label
  )
  private String pageHeader;
  // ==== end synthesised definition-only fields ====
}
