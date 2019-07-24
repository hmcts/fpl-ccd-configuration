package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CaseData {
    private final List<Element<MigratedRespondent>> respondents1;
    private final Respondents respondents;
    private final String respondentsMigrated;
}
