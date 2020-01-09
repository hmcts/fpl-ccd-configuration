package uk.gov.hmcts.reform.fpl.events.robotics;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
@EqualsAndHashCode(callSuper = true)
public class ResendFailedRoboticNotificationEvent extends RoboticsNotificationEvent {
    public ResendFailedRoboticNotificationEvent(CaseData caseData) {
        super(caseData);
    }
}
