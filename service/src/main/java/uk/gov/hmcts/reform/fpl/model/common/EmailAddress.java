package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailAddress {
    @NotBlank(message = "Enter an email address for the contact")
    private final String email;
    private final String emailUsageType;
}
