package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class RoboticsDataVerificationHelper {
    private RoboticsDataVerificationHelper() {
    }

    public static void runVerificationsOnRoboticsData(final RoboticsData roboticsData) {
        verifyOwningCourtCode(roboticsData);
    }

    public static void verifyRoboticsJsonData(final String roboticsJsonData) {
        if (isBlank(roboticsJsonData)) {
            RoboticsDataException roboticsDataException = new RoboticsDataException(
                "Robotics email notification failed to proceed as Json data is empty/null");

            log.error("", roboticsDataException);
            throw roboticsDataException;
        }
    }

    private static void verifyOwningCourtCode(final RoboticsData roboticsData) {
        if (roboticsData.getOwningCourt() == 0) {
            throw new RoboticsDataException(
                String.format("court code with value %s is invalid", roboticsData.getOwningCourt()));
        }
    }
}
