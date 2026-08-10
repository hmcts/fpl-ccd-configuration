package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class ApproveOrderUrgencyOption {

    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ReviewUrgency",
            typeParameterClass = ReviewUrgency.class
    )
    @Builder.Default
    private final List<YesNo> urgency = new ArrayList<>();

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<ul><li>EPOs, ICOs, recovery orders or port alerts</li><li>interpreter booking for hearings within 3 working days</li><li>production order for hearings within 10 working days</li><li>order against government departments (such as DWP, HMRC, Home Office)</li><li>any action that needs to be taken within 24 hours</li></ul>",
          typeOverride = FieldType.Label
  )
  private String urgencyLabel;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Call 020 3966 8650 if you need action within an hour.<br />For internal use only.<br />Monday to Friday from 9am to 5pm</strong></div>",
          typeOverride = FieldType.Label
  )
  private String urgencyLabel2;
  // ==== end synthesised definition-only fields ====
}
