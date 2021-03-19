package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@Jacksonized
@Valid
public class RespondentSolicitor {
    private String firstName;
    private String lastName;
    @NotNull(message = "Enter the respondent's solicitor email address")
    private String email;
    private Organisation organisation;
    private Address regionalOfficeAddress;
    private UnregisteredOrganisation unregisteredOrganisation;
}
