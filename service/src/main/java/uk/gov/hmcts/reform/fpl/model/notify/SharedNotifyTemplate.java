package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Getter
@Setter
public class SharedNotifyTemplate implements NotifyData {
    private List<String> ordersAndDirections;
    private String dataPresent = YES.getValue();
    private String fullStop = NO.getValue();
    private String timeFramePresent;
    private String timeFrameValue;
    private String urgentHearing;
    private String nonUrgentHearing;
    private String firstRespondentName;
    private String reference;
    private String caseUrl;
    private String localAuthority;
}
