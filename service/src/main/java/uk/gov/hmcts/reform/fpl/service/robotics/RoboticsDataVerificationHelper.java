package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.exceptions.OtherOrderTypeEmailNotificationException;
import uk.gov.hmcts.reform.fpl.exceptions.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;

@Slf4j
public class RoboticsDataVerificationHelper {

    private RoboticsDataVerificationHelper() {
    }

    public static void runVerificationsOnRoboticsData(final RoboticsData roboticsData) {
        verifyApplicationType(roboticsData.getApplicationType());
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

    private static void verifyApplicationType(final String applicationType) {
        if (OTHER.getLabel().equalsIgnoreCase(applicationType)) {
            String errorMessage = "Failed to send case submitted notification to Robotics as only "
                + "Other order type selected";

            OtherOrderTypeEmailNotificationException otherOrderTypeEmailNotificationException =
                new OtherOrderTypeEmailNotificationException(errorMessage);

            logEmailNotificationError(otherOrderTypeEmailNotificationException);

            throw otherOrderTypeEmailNotificationException;
        }
    }

    private static void logEmailNotificationError(final OtherOrderTypeEmailNotificationException exception) {
        String errorMessage = String.format("Email notification failed due to %1$s", exception.getMessage());
        log.error(errorMessage, exception);
    }
}
