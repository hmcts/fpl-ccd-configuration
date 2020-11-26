package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class BaseCaseNotifyData implements NotifyData {
    private final String respondentLastName;
    private final String caseUrl;
}
