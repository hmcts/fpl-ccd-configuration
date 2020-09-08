package uk.gov.hmcts.reform.fpl.model.notify.sdo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Builder
@EqualsAndHashCode
public class SDONotifyData implements NotifyData {
    private final String familyManCaseNumber;
    private final String leadRespondentsName;
    private final String hearingDate;
    private final String reference;
    private final String caseUrl;
    private final String title;
}
