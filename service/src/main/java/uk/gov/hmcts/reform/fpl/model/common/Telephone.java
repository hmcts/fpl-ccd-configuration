package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Telephone {
    private final String telephoneNumber;
    private final String telephoneUsageType;
    @NotBlank(message = "Enter the contact's full name")
    private final String contactDirection;
}
