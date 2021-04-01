package uk.gov.hmcts.reform.fpl.model.notify.sdo;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
public class CTSCTemplateForSDO implements NotifyData {
    private String respondentLastName;
    private String callout;
    private String courtName;
    private String caseUrl;
    private String documentLink;
    private String hearingNeedsPresent;
    private List<String> hearingNeeds;
}
