package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Telephone {
    @CCD(label = "Telephone number", hint = "For example, 020 2772 5772", max = 24)
    private final String telephoneNumber;
    @CCD(ignore = true)
    private final String telephoneUsageType;
    @CCD(label = "Name of person to contact")
    @NotBlank(message = "Enter the contact's full name")
    private final String contactDirection;
}
