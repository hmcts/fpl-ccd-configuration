package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Data
public class ApplicationFormRemovedNotifyData extends BaseCaseNotifyData {
    private final String caseName;
    private final String familyManCaseNumber;
    private final String removalReason;
}
