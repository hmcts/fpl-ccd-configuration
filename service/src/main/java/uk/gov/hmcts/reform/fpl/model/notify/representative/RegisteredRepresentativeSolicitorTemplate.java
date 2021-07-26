package uk.gov.hmcts.reform.fpl.model.notify.representative;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class RegisteredRepresentativeSolicitorTemplate implements NotifyData {
    String salutation;
    String localAuthority;
    String clientFullName;
    String ccdNumber;
    String caseName;
    String manageOrgLink;
    String childLastName;
}
