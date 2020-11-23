package uk.gov.hmcts.reform.fpl.model.notify.sdo;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Setter
public class OrderRemovalTemplate implements NotifyData {
    private String caseReference;
    private String caseUrl;
    private String respondentLastName;
    private String returnedNote;
}
