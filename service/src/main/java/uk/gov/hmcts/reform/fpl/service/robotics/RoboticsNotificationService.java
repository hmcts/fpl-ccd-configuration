package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.exceptions.robotics.RoboticsDataException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import static java.util.Set.of;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsNotificationService {
    private final EmailService emailService;
    private final RoboticsDataService roboticsDataService;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;

    @EventListener
    public void notifyRoboticsOfSubmittedCaseData(final CaseNumberAdded event) {
        sendSubmittedCaseData(event.getCaseData());
    }

    public void sendSubmittedCaseData(final CaseData caseData) {
        if (isNotEmpty(caseData)) {
            try {
                RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

                EmailData emailData = prepareEmailData(roboticsData);

                emailService.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
                log.info("Robotics email notification successful for case with caseId {} and familyManNumber {}",
                    caseData.getId(), caseData.getFamilyManCaseNumber());

            } catch (Exception exc) {
                log.error("Robotics email notification failed for case with caseId {} and familyManNumber {} due to {}",
                    caseData.getId(), caseData.getFamilyManCaseNumber(), exc.getMessage());

                throw exc;
            }
        }
    }

    private EmailData prepareEmailData(final RoboticsData roboticsData) {
        final String roboticsJsonData = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        verifyRoboticsJsonData(roboticsJsonData);

        final String fileNamePrefix = "CaseSubmitted_";
        final String fileName = join(fileNamePrefix, roboticsData.getCaseNumber());
        final String fileNameAndExtension = join(fileName, ".json");

        return EmailData.builder()
            .message("")
            .subject(fileName)
            .recipient(roboticsEmailConfiguration.getRecipient())
            .attachments(of(json(roboticsJsonData.getBytes(), fileNameAndExtension)))
            .build();
    }

    private void verifyRoboticsJsonData(final String roboticsJsonData) {
        if (isBlank(roboticsJsonData)) {
            throw new RoboticsDataException(
                "Robotics email notification failed to proceed as Json data is empty/null");
        }
    }
}
