package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.events.robotics.RoboticsNotificationEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseNumberAdded extends RoboticsNotificationEvent {
    public CaseNumberAdded(CaseData caseData) {
        super(caseData);
    }
}
