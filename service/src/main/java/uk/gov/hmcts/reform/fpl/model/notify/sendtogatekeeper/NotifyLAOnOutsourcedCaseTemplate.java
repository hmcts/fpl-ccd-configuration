package uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public final class NotifyLAOnOutsourcedCaseTemplate extends SharedNotifyTemplate {
    private String thirdParty;
}
