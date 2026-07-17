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
public class GroundsForEducationSupervisionOrder {
    @CCD(
            label = "*State your reason(s) for believing the grounds exist. If you are relying on a report or other documentary evidence, state the date(s) and author(s) and attach a copy in the \"Upload documents\" section.",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Enter details for believing the ground exists")
    private final String groundDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "The ground is that the child[ren] [is] [are] of compulsory school age and [is] [are] not being properly educated.",
          typeOverride = FieldType.Label
  )
  private String groundHeader;
  // ==== end synthesised definition-only fields ====
}
