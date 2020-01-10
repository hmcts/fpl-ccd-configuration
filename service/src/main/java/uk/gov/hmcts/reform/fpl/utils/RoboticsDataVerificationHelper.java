package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.exceptions.OtherOrderTypeEmailNotificationException;
import uk.gov.hmcts.reform.fpl.exceptions.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class RoboticsDataVerificationHelper {
    private static final String OTHER_TYPE_LABEL_VALUE = "Discharge of care";

    private RoboticsDataVerificationHelper() {
    }

    public static void runVerificationsOnRoboticsData(final RoboticsData roboticsData) {
        logOtherOrderTypeApplicationType(roboticsData.getApplicationType());
        verifyOwningCourtCode(roboticsData.getOwningCourt());
    }

    public static void verifyRoboticsJsonData(final String roboticsJsonData) {
        if (isBlank(roboticsJsonData)) {
            throw new RoboticsDataException("Failed to proceed as Json data is empty/null");
        }
    }

    private static void verifyOwningCourtCode(final int owningCourtCode) {
        if (owningCourtCode == 0) {
            throw new RoboticsDataException(String.format("Failed to send as cort code with value %s is invalid",
                owningCourtCode));
        }
    }

    private static void logOtherOrderTypeApplicationType(final String applicationType) {
        if (OTHER_TYPE_LABEL_VALUE.equalsIgnoreCase(applicationType)) {
            String errorMessage = "sending case submitted notification to Robotics with only "
                + "Other order type selected";

            OtherOrderTypeEmailNotificationException otherOrderTypeEmailNotificationException =
                new OtherOrderTypeEmailNotificationException(errorMessage);

            logEmailNotificationError(otherOrderTypeEmailNotificationException);
        }
    }

    private static void logEmailNotificationError(final RuntimeException exception) {
        String errorMessage = String.format("Email notification failed due to %1$s", exception.getMessage());
        log.error(errorMessage, exception);
    }
}
