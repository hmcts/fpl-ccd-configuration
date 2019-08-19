package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;

@Data
@Builder
@AllArgsConstructor
public class Applicant {
    @Valid
    private final ApplicantParty party;
    private final String leadApplicantIndicator;
}
