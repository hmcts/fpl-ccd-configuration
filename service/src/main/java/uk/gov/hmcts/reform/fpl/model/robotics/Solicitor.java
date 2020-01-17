package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Solicitor {
    private final String firstName;
    private final String lastName;
}
