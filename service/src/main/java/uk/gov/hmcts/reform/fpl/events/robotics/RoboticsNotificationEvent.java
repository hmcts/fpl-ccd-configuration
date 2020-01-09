package uk.gov.hmcts.reform.fpl.events.robotics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class RoboticsNotificationEvent {
    private final CaseData caseData;
}
