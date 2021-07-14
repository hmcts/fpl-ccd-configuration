package uk.gov.hmcts.reform.fpl.model.notify.noticeofchange;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class NoticeOfChangeRespondentSolicitorTemplate implements NotifyData {
    private final String salutation;
    private String caseName;
    private String ccdNumber;
    private String caseUrl;
    private String clientFullName;
    private String childLastName;
}
