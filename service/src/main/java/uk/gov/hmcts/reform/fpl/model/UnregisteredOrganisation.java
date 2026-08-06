package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class UnregisteredOrganisation {
    @CCD(label = "Organisation name")
    private String name;
    @CCD(label = "Organisation address", typeOverride = FieldType.AddressUK)
    private Address address;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## Ask an organisation to register",
          showCondition = "signUpRequestHeader=\"HIDE_LABEL\"",
          typeOverride = FieldType.Label
  )
  private String signUpRequestHeader;
  @CCD(
          label = "If the organisation you need isn’t listed, enter their details to send them a sign-up request",
          showCondition = "signupRequestLabel=\"HIDE_LABEL\"",
          typeOverride = FieldType.Label
  )
  private String signupRequestLabel;
  // ==== end synthesised definition-only fields ====
}
