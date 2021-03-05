package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class NewJudicialMessageTemplate extends BaseCaseNotifyData {
    private final String callout;
    private final String sender;
    private final String urgency;
    private final String latestMessage;
    private final String hasUrgency;
}
