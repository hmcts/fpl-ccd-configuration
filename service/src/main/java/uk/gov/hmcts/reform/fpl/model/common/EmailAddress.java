package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidEmailGroup;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidEmailAddress;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailAddress {
    @Email(
        message = "Enter a valid email address",
        regexp = "^[_A-Za-z0-9-']+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    @NotBlank(message = "Enter an email address for the contact")
    @IsValidEmailAddress(groups = ValidEmailGroup.class)
    private final String email;
    private final String emailUsageType;
}
