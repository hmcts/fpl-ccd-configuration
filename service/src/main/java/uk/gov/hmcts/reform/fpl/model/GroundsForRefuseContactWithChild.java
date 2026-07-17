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
public class GroundsForRefuseContactWithChild {
    @CCD(
            label = "State the full name(s) of each person who has contact with each child and the current arrangements for contact",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please state the full name(s) of each person who has contact with each child "
                        + "and the current arrangements for contact")
    private String personHasContactAndCurrentArrangement;

    @CCD(
            label = "State whether the local authority has refused contact for 7 days or less (Section 34(6) Children Act 1989)",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please state whether the local authority has refused contact for 7 days or less")
    private String laHasRefusedContact;

    @CCD(
            label = "State the full name and relationship of any person in respect of whom authority to refuse contact with each child is sought",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please state the full name and relationship of any person in respect of whom authority to "
                        + "refuse contact with each child is sought")
    private String personsBeingRefusedContactWithChild;

    @CCD(
            label = "*If you are relying on a report or other documentary evidence state the date(s) and author(s) and enclose a copy in the upload documents section",
            typeOverride = FieldType.TextArea
    )
    @NotBlank(message = "Please provide reasons for application")
    private String reasonsOfApplication;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "<h2>Current arrangement for contact</h2>", typeOverride = FieldType.Label)
  private String currentArrangementHeader;
  @CCD(label = "<h2>The order applied for</h2>", typeOverride = FieldType.Label)
  private String orderAppliedForHeader;
  @CCD(label = "<h2>Reasons for application</h2>", typeOverride = FieldType.Label)
  private String reasonsOfApplicationHeader;
  // ==== end synthesised definition-only fields ====
}
