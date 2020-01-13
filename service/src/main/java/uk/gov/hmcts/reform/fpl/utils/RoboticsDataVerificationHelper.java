package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsLoggerHelper.logAndThrowException;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsLoggerHelper.logOtherOrderTypeApplicationType;

@Slf4j
public class RoboticsDataVerificationHelper {
    private RoboticsDataVerificationHelper() {
    }

    public static void runVerificationsOnRoboticsData(final RoboticsData roboticsData) {
        logOtherOrderTypeApplicationType(roboticsData);
        verifyOwningCourtCode(roboticsData);
    }

    public static void verifyRoboticsJsonData(final String roboticsJsonData) {
        if (isBlank(roboticsJsonData)) {
            RoboticsDataException roboticsDataException = new RoboticsDataException(
                "Failed to proceed as Json data is empty/null");

            log.error("", roboticsDataException);
            throw roboticsDataException;
        }
    }

    private static void verifyOwningCourtCode(final RoboticsData roboticsData) {
        if (roboticsData.getOwningCourt() == 0) {
            RoboticsDataException roboticsDataException = new RoboticsDataException(
                String.format("court code with value %s is invalid", roboticsData.getOwningCourt()));

            logAndThrowException(roboticsDataException, roboticsData);
        }
    }
}
