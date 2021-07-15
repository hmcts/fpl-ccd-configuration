package uk.gov.hmcts.reform.fpl.model.notify.representative;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class UnregisteredRepresentativeSolicitorTemplate implements NotifyData {
    String ccdNumber;
    String localAuthority;
    String clientFullName;
    String caseName;
    String childLastName;
}
