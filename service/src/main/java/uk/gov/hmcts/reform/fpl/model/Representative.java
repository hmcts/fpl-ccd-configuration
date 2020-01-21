package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;

@Data
@Builder
@AllArgsConstructor
public class Representative {

    private final String fullName;

    private final String positionInACase;

    private final String email;

    private final String telephoneNumber;

    private final Address address;

    private final RepresentativeServingPreferences servingPreferences;

    private final RepresentativeRole role;
}
