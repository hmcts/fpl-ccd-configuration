package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class RespondQueryNotifyData implements NotifyData {
    private final String caseId;
    private final String caseName;
    private final String caseUrl;
    private final String queryDate;
}
