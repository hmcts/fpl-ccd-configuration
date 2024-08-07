package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Applicant {
    @Valid
    @NotNull(message = "You need to add details to applicant")
    private final ApplicantParty party;
    private final String leadApplicantIndicator;
}
