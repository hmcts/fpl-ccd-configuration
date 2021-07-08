package uk.gov.hmcts.reform.fpl.model.notify.payment;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class FailedPBANotificationData implements NotifyData {
    private String caseUrl;
    private String applicationType;
    private String applicant;
}
