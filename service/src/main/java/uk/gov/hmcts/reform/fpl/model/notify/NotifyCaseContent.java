package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.List;

@Data
@Builder
public class NotifyCaseContent implements NotifyData {
    private final List<String> ordersAndDirections;
    private final YesNo dataPresent;
    private final YesNo fullStop;
    private final YesNo timeFramePresent;
    private final YesNo urgentHearing;
    private final YesNo nonUrgentHearing;
    private final String firstRespondentName;
    private final String reference;
    private final String caseUrl;
}
