package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@SuperBuilder(toBuilder = true)
public class SharedNotifyTemplate implements NotifyData {
    private List<String> ordersAndDirections;
    @Builder.Default
    private String dataPresent = YES.getValue();
    @Builder.Default
    private String fullStop = NO.getValue();
    private String timeFramePresent;
    private String timeFrameValue;
    private String urgentHearing;
    private String nonUrgentHearing;
    private String firstRespondentName;
    private String reference;
    private String caseUrl;
    private Map<String, Object> documentLink;
    private String localAuthority;
}
