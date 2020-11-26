package uk.gov.hmcts.reform.fpl.model.notify.submittedcase;

import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@SuperBuilder
@Setter
public final class SubmitCaseHmctsTemplate extends SharedNotifyTemplate {
    private String court;
}
