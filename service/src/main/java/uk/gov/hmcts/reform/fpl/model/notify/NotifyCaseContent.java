package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.util.List;

@Setter
@Getter
public class NotifyCaseContent implements NotifyData {
    private List<String> ordersAndDirections;
    private YesNo dataPresent;
    private YesNo fullStop;
    private YesNo timeFramePresent;
    private YesNo urgentHearing;
    private YesNo nonUrgentHearing;
    private String firstRespondentName;
    private String reference;
    private String caseUrl;
}
