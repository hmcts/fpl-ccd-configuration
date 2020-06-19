package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@ComplexType(name = "Applicants")
public class Applicant {
    @Valid
    @NotNull(message = "You need to add details to applicant")
    private final ApplicantParty party;
    private final String leadApplicantIndicator;
}
