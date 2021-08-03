package uk.gov.hmcts.reform.fpl.model.notify.legalcounsel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder
public class LegalCounsellorRemovedNotifyTemplate extends SharedNotifyTemplate {
    private final String caseName;
    private final String childLastName;
    private final String salutation;
    private final String clientFullName;
    private final String ccdNumber;
}
