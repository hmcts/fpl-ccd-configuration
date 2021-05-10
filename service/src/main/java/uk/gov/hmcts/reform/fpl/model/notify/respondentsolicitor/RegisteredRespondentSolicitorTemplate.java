package uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Value
@Builder
public class RegisteredRespondentSolicitorTemplate implements NotifyData {
    String salutation;
    String localAuthority;
}
