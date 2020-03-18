package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Solicitor {
    private final String dx;
    @NotBlank(message = "Enter the solicitor's full name")
    private final String name;

    @NotBlank(message = "Enter the solicitor's email")
    @Email(
        message = "Enter a valid email address",
        regexp = "^[_A-Za-z0-9-']+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    private final String email;

    private final String mobile;
    private final String reference;
    private final String telephone;
}
