package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;

@Value
@Builder(toBuilder = true)
public class LegalRepresentative {

    String fullName;
    LegalRepresentativeRole role;
    String organisation;
    String email;
    String telephoneNumber;

}
