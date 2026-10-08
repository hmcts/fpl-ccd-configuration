package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Builder
@Getter
public class C2ApplicationRejectedTemplate implements NotifyData {
    private final String respondentLastName;
    private final String subjectLineWithHearingDate;
    private final String caseUrl;
    private final String orderList;
}
