package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseSubmittedEvent;
import uk.gov.hmcts.reform.fpl.exceptions.OtherOrderTypeEmailNotificationException;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import static java.util.Set.of;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsNotificationService {
    private final EmailService emailService;
    private final RoboticsDataService roboticsDataService;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;

    @EventListener
    public void notifyRoboticsOfSubmittedCaseData(final CaseSubmittedEvent event) {
        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(event.getCaseData());

        EmailData emailData = prepareEmailData(roboticsData);
        emailService.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(final RoboticsData roboticsData) {
        verifyRoboticsData(roboticsData);

        final String fileNamePrefix = "CaseSubmitted_";
        final String fileName = join(fileNamePrefix, roboticsData.getCaseNumber());
        final String fileNameAndExtension = join(fileName, ".json");

        final String roboticsJsonData = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        return EmailData.builder()
            .message("")
            .subject(fileName)
            .to(roboticsEmailConfiguration.getRecipient())
            .attachments(of(json(roboticsJsonData.getBytes(), fileNameAndExtension)))
            .build();
    }

    private void verifyRoboticsData(final RoboticsData roboticsData) {
        if (OTHER.getLabel().equalsIgnoreCase(roboticsData.getApplicationType())) {
            String errorMessage = "Failed to send case submitted notification to Robotics as only "
                + "Other order type selected";

            OtherOrderTypeEmailNotificationException otherOrderTypeEmailNotificationException =
                new OtherOrderTypeEmailNotificationException(errorMessage);

            logOtherOrderTypeRoboticsEmailNotificationError(otherOrderTypeEmailNotificationException,
                roboticsDataService.convertRoboticsDataToJson(roboticsData));

            throw otherOrderTypeEmailNotificationException;
        }
    }

    private void logOtherOrderTypeRoboticsEmailNotificationError(final OtherOrderTypeEmailNotificationException
                                                                     exception,
                                                                 final String roboticsJsonData) {
        String errorMessage = String.format("Email with details [%1$s] notification failed due to %2$s",
            roboticsJsonData, exception.getMessage());

        log.error(errorMessage, exception);
    }
}
