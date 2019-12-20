package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Address;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Respondent {
    private final String firstName;
    private final String lastName;
    private final String gender;
    private final Address address;
    private final String relationshipToChild;
    private final String dob;
    private final boolean confidential;
}
