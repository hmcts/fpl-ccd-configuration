package uk.gov.hmcts.reform.fpl.model;

import uk.gov.hmcts.ccd.sdk.types.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Solicitor {
    private final String dx;
    @NotBlank(message = "Enter the solicitor's full name")
    private final String name;
    @NotBlank(message = "Enter the solicitor's email")
    private final String email;
    private final String mobile;
    private final String reference;
    private final String telephone;
    private FieldType t;
}
