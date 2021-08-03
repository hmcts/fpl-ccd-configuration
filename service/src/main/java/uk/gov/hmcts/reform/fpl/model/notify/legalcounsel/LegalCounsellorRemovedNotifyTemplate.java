package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@EqualsAndHashCode
@ToString(callSuper = true)
@Data
@SuperBuilder
public class LegalCounsellorRemovedNotifyTemplate implements NotifyData {
    private final String caseName;
    private final String childLastName;
    private final String salutation;
    private final String clientFullName;
    private final String ccdNumber;
}
