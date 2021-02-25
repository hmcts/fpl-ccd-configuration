package uk.gov.hmcts.reform.fpl.model.notify.submittedcase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
public final class OutsourcedCaseTemplate extends SharedNotifyTemplate {
    private String thirdParty;
}
