package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Applicant {
    private final String name;
    private final String contactName;
    private final String jobTitle;
    private final Address address;
    private final String mobileNumber;
    private final String telephoneNumber;
    private final String email;
}
