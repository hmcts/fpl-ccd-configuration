package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class NewJudicialMessageReplyTemplate extends BaseCaseNotifyData {
    private final String callout;
    private final String note;
}
