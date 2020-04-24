package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SharedNotifyTemplate implements NotifyData {
    private List<String> ordersAndDirections;
    private String dataPresent;
    private String fullStop;
    private String timeFramePresent;
    private String timeFrameValue;
    private String urgentHearing;
    private String nonUrgentHearing;
    private String firstRespondentName;
    private String reference;
    private String caseUrl;
    private String localAuthority;
}
