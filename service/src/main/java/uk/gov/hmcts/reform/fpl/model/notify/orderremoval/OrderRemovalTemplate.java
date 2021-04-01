package uk.gov.hmcts.reform.fpl.model.notify.orderremoval;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Builder
@Data
public class OrderRemovalTemplate implements NotifyData {
    private String caseReference;
    private String caseUrl;
    private String respondentLastName;
    private String removalReason;
}
