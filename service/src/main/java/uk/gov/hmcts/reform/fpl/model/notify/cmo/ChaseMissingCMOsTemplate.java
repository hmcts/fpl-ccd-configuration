package uk.gov.hmcts.reform.fpl.model.notify.cmo;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Data
@Builder
public class ChaseMissingCMOsTemplate implements NotifyData {

    private final String respondentLastName;
    private final String subjectLine;
    private final String listOfHearingsMissingOrders;
    private final String caseUrl;

}
