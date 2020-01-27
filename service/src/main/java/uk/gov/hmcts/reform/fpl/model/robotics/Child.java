package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Child {
    private final String firstName;
    private final String lastName;
    private final String gender;
    private final String dob;
    private final boolean isParty;
}
