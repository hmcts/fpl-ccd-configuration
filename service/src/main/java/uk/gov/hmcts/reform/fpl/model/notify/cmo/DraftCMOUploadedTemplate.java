package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Accessors(chain = true)
@Getter
@Setter
public class DraftCMOUploadedTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String subjectLineWithHearingDate;
    private String respondentLastName;
    private String caseUrl;
}
