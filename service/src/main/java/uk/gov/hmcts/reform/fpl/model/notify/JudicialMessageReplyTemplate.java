package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class JudicialMessageReplyTemplate extends BaseCaseNotifyData {
    private final String callout;
    private final String latestMessage;
    private final String hasApplication;
    private final String applicationType;
}
