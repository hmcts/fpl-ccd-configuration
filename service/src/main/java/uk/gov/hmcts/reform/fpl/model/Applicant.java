package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Applicant {
    private final ApplicantParty party;
    private final String leadApplicantIndicator;
}
