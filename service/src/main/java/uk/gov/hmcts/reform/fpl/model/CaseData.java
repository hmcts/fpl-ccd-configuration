package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CaseData {
    private final OldChildren children;
    private final List<Element<Child>> children1;
    private final String childrenMigrated;
    private final List<Element<Applicant>> applicants;
    private final List<Element<Respondent>> respondents;
}
