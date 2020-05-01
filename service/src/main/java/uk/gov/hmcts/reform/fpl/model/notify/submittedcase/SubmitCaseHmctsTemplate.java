package uk.gov.hmcts.reform.fpl.model.notify.submittedcase;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public final class SubmitCaseHmctsTemplate extends SharedNotifyTemplate {
    private String court;
}
