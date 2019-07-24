package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude
public class CaseData {
    private final OldApplicant applicant;
    private final List<Element<Applicant>> applicants;
    private final String applicantsMigrated;

    public CaseData(OldApplicant applicant, List<Element<Applicant>> applicants, String applicantsMigrated) {
        this.applicant = applicant;
        this.applicants = applicants;
        this.applicantsMigrated = applicantsMigrated;
    }
}
