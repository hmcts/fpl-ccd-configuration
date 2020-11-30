package uk.gov.hmcts.reform.fpl.model.notify.orderremoval;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@SuperBuilder
@Data
public class OrderRemovalTemplate implements NotifyData {
    private String caseReference;
    private String caseUrl;
    private String respondentLastName;
    private String returnedNote;
}
