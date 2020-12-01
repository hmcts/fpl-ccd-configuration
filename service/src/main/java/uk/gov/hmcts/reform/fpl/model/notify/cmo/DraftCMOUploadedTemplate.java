package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@SuperBuilder
public class DraftCMOUploadedTemplate implements NotifyData {
    private final String judgeTitle;
    private final String judgeName;
    private final String subjectLineWithHearingDate;
    private final String respondentLastName;
    private final String caseUrl;
}
