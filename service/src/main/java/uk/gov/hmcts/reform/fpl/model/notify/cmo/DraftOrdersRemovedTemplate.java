package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.cafcass.CafcassData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class DraftOrdersRemovedTemplate implements NotifyData, CafcassData {
    private final String judgeTitle;
    private final String judgeName;
    private final String subjectLineWithHearingDate;
    private final String respondentLastName;
    private final String caseUrl;
    private final String draftOrdersRemoved;
    private final String removalReason;
}
