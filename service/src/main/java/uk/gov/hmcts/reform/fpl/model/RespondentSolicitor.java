package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import javax.validation.constraints.NotBlank;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class RespondentSolicitor {
    private String firstName;
    private String lastName;
    @NotBlank(message = "Enter the respondent's solicitor email address")
    private String email;
    private Organisation organisation;
    private AdditionalOrgDetails additionalOrgDetails;

}
