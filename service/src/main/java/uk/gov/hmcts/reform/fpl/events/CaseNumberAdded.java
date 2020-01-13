package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.robotics.RoboticsNotificationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseNumberAdded extends RoboticsNotificationEvent {
    public CaseNumberAdded(CaseDetails caseDetails) {
        super(caseDetails);
    }
}
