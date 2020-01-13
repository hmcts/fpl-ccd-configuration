package uk.gov.hmcts.reform.fpl.events.robotics;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class RoboticsNotificationEvent {
    private final CaseDetails caseDetails;
}
