package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.OtherOrderTypeEmailNotificationException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import static java.lang.String.format;

@Slf4j
public class RoboticsLoggerHelper {
    private static final String OTHER_TYPE_LABEL_VALUE = "Discharge of care";

    private RoboticsLoggerHelper() {
    }

    public static void logAndThrowException(final RuntimeException roboticsException, final RoboticsData roboticsData) {
        logEmailNotificationError(roboticsException, roboticsData.getCaseNumber(), roboticsData.getCaseId());
        throw roboticsException;
    }

    public static void logOtherOrderTypeApplicationType(final RoboticsData roboticsData) {
        if (OTHER_TYPE_LABEL_VALUE.equalsIgnoreCase(roboticsData.getApplicationType())) {
            String errorMessage = "sending case submitted notification to Robotics with only "
                + "Other order type selected";

            OtherOrderTypeEmailNotificationException otherOrderTypeEmailNotificationException =
                new OtherOrderTypeEmailNotificationException(errorMessage);

            logEmailNotificationError(otherOrderTypeEmailNotificationException, roboticsData.getCaseNumber(),
                roboticsData.getCaseId());
        }
    }

    public static void logEmailNotificationError(final RuntimeException exception, final String familyManNumber,
                                                  final Long caseId) {
        String errorMessage =
            format("Robotics email notification failed for case with caseId %1$s and familyManNumber %2$s due to %3$s",
                caseId, familyManNumber, exception.getMessage());
        log.error(errorMessage, exception);
    }
}
