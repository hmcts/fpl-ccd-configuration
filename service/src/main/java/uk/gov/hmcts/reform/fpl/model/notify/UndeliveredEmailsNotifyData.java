package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UndeliveredEmailsNotifyData implements NotifyData {
    String emails;
}
