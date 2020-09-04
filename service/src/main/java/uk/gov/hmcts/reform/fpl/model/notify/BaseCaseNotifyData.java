package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public class BaseCaseNotifyData implements NotifyData {
    private final String respondentLastName;
    private final String caseUrl;
}
