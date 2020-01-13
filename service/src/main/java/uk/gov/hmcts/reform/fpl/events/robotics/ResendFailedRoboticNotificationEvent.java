package uk.gov.hmcts.reform.fpl.events.robotics;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Value
@EqualsAndHashCode(callSuper = true)
public class ResendFailedRoboticNotificationEvent extends RoboticsNotificationEvent {
    public ResendFailedRoboticNotificationEvent(CaseDetails caseDetails) {
        super(caseDetails);
    }
}
