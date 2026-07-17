package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Solicitor {
    @CCD(ignore = true)
    private final String firstName;
    @CCD(ignore = true)
    private final String lastName;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "# Solicitor's details", showCondition = "solicitorLabel=\"HIDE_LABEL\"", typeOverride = FieldType.Label)
  private String solicitorLabel;
  @CCD(
          label = "Solicitor's full name",
          hint = "If you're applying for a local authority, add details of the legal representative in your organisation who'll manage this case"
  )
  private String name;
  @CCD(label = "Solicitor's mobile number", hint = "For example, 07665 545327", max = 24)
  private String mobile;
  @CCD(label = "Solicitor's telephone number", hint = "For example, 020 2772 5772", typeOverride = FieldType.PhoneUK)
  private String telephone;
  @CCD(
          label = "Solicitor's email",
          hint = "Case notifications will go to this email address",
          typeOverride = FieldType.Email
  )
  private String email;
  @CCD(label = "DX number")
  private String dx;
  @CCD(label = "Solicitor's reference")
  private String reference;
  // ==== end synthesised definition-only fields ====
}
