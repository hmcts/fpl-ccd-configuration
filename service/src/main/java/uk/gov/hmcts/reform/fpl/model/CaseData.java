package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseData {
    private final OldApplicant applicant;
    private final List<Element<Applicant>> applicants;
    private final String applicantsMigrated;
}
