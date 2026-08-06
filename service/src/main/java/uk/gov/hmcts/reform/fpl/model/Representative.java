package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Representative implements Recipient {

    @CCD(label = "Full name")
    private final String fullName;

    @CCD(label = "Position in a case")
    private final String positionInACase;

    @CCD(label = "Email address", typeOverride = FieldType.Email)
    private final String email;

    @CCD(label = "Phone number")
    private final String telephoneNumber;

    @CCD(label = " ", hint = "Address", typeOverride = FieldType.AddressUK)
    private final Address address;

    @CCD(label = "How do they want to get case information?")
    private final RepresentativeServingPreferences servingPreferences;

    @CCD(label = "Who are they?", typeOverride = FieldType.FixedList, typeParameterOverride = "RepresentativeRole")
    private final RepresentativeRole role;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Idam id", showCondition = "idamId=\"DO_NOT_SHOW\"")
  private String idamId;
  // ==== end synthesised definition-only fields ====
}
