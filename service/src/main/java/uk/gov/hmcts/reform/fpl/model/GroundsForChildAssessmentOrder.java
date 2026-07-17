package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class GroundsForChildAssessmentOrder {
    @CCD(
            label = "State your reason(s) for believing the grounds exist. \n If you are relying on a report or other documentary evidence, state the date(s) and author(s) and attach a copy in the \"Upload documents\" section.",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Enter details of how the case meets the threshold criteria")
    private final String thresholdDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The grounds are that there is reasonable cause to suspect that the child[ren] [is] [are] suffering, or [is] [are] likely to suffer, significant harm",
          typeOverride = FieldType.Label
  )
  private String thresholdReason;
  @CCD(
          label = "**and** \n an assessment of the child[ren]'s health or development or of the way in which the child[ren] [has][have] been treated, is required to determine whether or not the child[ren] [is][are] suffering, or [is][are] likely to suffer, significant harm ",
          typeOverride = FieldType.Label
  )
  private String thresholdReason1;
  @CCD(
          label = "**and** \n it is unlikely that such an assessment will be made, or be satisfactory, in the absence of an order under this section.",
          typeOverride = FieldType.Label
  )
  private String thresholdReason2;
  // ==== end synthesised definition-only fields ====
}
