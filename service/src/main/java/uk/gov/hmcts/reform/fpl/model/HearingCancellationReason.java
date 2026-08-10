package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsLocalAuthority;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsLawyers;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsCafcass;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsLegalAidAgency;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsHMCTS;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsExperts;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReasonsOtherV2;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CancellationReason", generate = true)
@Data
@Builder
public class HearingCancellationReason {
    @CCD(
            label = "Which authorities or bodies cannot meet their hearing commitments?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingCancellationReasonType",
            typeParameterClass = HearingCancellationReasonType.class
    )
    private final String type;
    @CCD(label = " ", showCondition = "reason=\"DO NOT SHOW\"")
    private final String reason;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonProperty("reason-LocalAuthority")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"LocalAuthority\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-LocalAuthority"
  )
  private HearingCancellationReasonsLocalAuthority reason_LocalAuthority;
  @JsonProperty("reason-Lawyers")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"Lawyers\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-Lawyers"
  )
  private HearingCancellationReasonsLawyers reason_Lawyers;
  @JsonProperty("reason-Cafcass")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"Cafcass\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-Cafcass"
  )
  private HearingCancellationReasonsCafcass reason_Cafcass;
  @JsonProperty("reason-LegalAidAgency")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"LegalAidAgency\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-LegalAidAgency"
  )
  private HearingCancellationReasonsLegalAidAgency reason_LegalAidAgency;
  @JsonProperty("reason-HMCTS")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"HMCTS\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-HMCTS"
  )
  private HearingCancellationReasonsHMCTS reason_HMCTS;
  @JsonProperty("reason-Experts")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"Experts\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-Experts"
  )
  private HearingCancellationReasonsExperts reason_Experts;
  @JsonProperty("reason-Other")
  @CCD(
          label = "Why was the hearing postponed?",
          showCondition = "type=\"Other\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingCancellationReasons-OtherV2"
  )
  private HearingCancellationReasonsOtherV2 reason_Other;
  // ==== end synthesised definition-only fields ====
}
