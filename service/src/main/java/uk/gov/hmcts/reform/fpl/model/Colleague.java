package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class Colleague {
    private final ColleagueRole role;
    private final String title;
    private final String fullName;
    private final String email;
    private final String phone;
}
