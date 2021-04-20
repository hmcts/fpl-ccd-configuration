package uk.gov.hmcts.reform.fpl.model.notify.submittedcase;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@SuperBuilder(toBuilder = true)
public final class RespondentSolicitorTemplate implements NotifyData {
    private final String salutation;
    private final String localAuthority;
}
