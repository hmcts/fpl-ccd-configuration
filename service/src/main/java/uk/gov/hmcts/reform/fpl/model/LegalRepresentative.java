package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;

@Value
@Builder(toBuilder = true)
public class LegalRepresentative {

    private final String fullName;
    private final LegalRepresentativeRole role;
    private final String organisation;
    private final String email;
    private final String telephoneNumber;

}
