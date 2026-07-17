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
public class GroundsForContactWithChild {
    @CCD(label = "Please state whether you are a parent or a guardian", typeOverride = FieldType.TextArea)
    @NotBlank(message = "Please state whether you are a parent or a guardian")
    private String parentOrGuardian;

    @CCD(
            label = "Please state whether you hold a residence order which was in force immediately before the care order was made (Section 34(1)(c) Children Act 1989)",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please state whether you hold a residence order which was in force "
                        + "immediately before the care order was made (Section 34(1)(c) Children Act 1989)")
    private String residenceOrder;

    @CCD(
            label = "Please state whether you had care of the child(ren) through an order which was in force immediately before the care order was made (Section 34(1)(d) Children Act 1989)",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please state whether you had care of the child(ren) through an order which was in force "
                        + "immediately before the care order was made (Section 34(1)(d) Children Act 1989)")
    private String hadCareOfChildrenBeforeCareOrder;

    @CCD(
            label = "If you are relying on a report or other documentary evidence state the date(s) and author(s) and enclose a copy in the upload documents section",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please provide reasons for application")
    private String reasonsForApplication;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "<h2>Your relationship to the child(ren)</h2>", typeOverride = FieldType.Label)
  private String relationshipToChildrenHeader;
  @CCD(label = "<h2>The order applied for and your reason(s) for the application</h2>", typeOverride = FieldType.Label)
  private String reasonsForApplicationHeader;
  // ==== end synthesised definition-only fields ====
}
