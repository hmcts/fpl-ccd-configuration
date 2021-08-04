package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class LegalCounsellorRemovedNotifyTemplate implements NotifyData {
    private final String caseName;
    private final String childLastName;
    private final String salutation;
    private final String clientFullName;
    private final String ccdNumber;
}
