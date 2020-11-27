package uk.gov.hmcts.reform.fpl.model.notify.submittedcase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public final class SubmitCaseHmctsTemplate extends SharedNotifyTemplate {
    private String court;
}
