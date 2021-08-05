package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class LegalCounsellorRemovedNotifyTemplate implements NotifyData {
    String caseName;
    String childLastName;
    String salutation;
    String clientFullName;
    String ccdNumber;
}
