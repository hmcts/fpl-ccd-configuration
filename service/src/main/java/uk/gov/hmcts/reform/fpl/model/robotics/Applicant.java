package uk.gov.hmcts.reform.fpl.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Applicant {
    private final String name;
    private final String contactName;
    private final String jobTitle;
    private final Address address;

    @Pattern(regexp = "^\\+?\\d{10,24}$", message = "is invalid")
    private final String mobileNumber;

    @Pattern(regexp = "^\\+?\\d{10,24}$", message = "is invalid")
    private final String telephoneNumber;

    @Email(message = "is invalid")
    private final String email;
}
