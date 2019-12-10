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

    private String idamId;

    private String fullName;

    private String positionInACase;

    private String email;

    private String telephoneNumber;

    private Address address;

    private RepresentativeServingPreferences servingPreferences;

    private RepresentativeRole role;
}
