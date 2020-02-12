package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Applicant {
    private final String name;
    private final String contactName;
    private final String jobTitle;
    private final Address address;

    @Pattern(regexp = "^\\+?\\d+$", message = "is invalid")
    @Size(max = 24, message = "exceeds maximum number of characters")
    private final String mobileNumber;

    @Pattern(regexp = "^\\+?\\d+$", message = "is invalid")
    @Size(max = 24, message = "exceeds maximum number of characters")
    private final String telephoneNumber;

    private final String email;
}
