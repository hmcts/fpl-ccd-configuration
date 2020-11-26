package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Accessors(chain = true)
@Data
@SuperBuilder
public class CMOReadyToSealTemplate implements NotifyData {
    private String judgeTitle;
    private String judgeName;
    private String subjectLineWithHearingDate;
    private String respondentLastName;
    private String caseUrl;
}
