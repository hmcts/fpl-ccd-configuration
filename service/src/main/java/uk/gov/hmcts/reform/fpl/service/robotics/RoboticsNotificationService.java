package uk.gov.hmcts.reform.fpl.service.robotics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import static java.util.Set.of;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsDataVerificationHelper.runVerificationsOnRoboticsData;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsDataVerificationHelper.verifyRoboticsJsonData;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "feature.toggle", name = "robotics.case-number.notification.enabled",
    havingValue = "true")
public class RoboticsNotificationService {
    private final EmailService emailService;
    private final RoboticsDataService roboticsDataService;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;
    private final ObjectMapper mapper;

    @EventListener
    public void notifyRoboticsOfSubmittedCaseData(final CaseNumberAdded event) {
        sendSubmittedCaseData(event.getCaseDetails());
    }

    public void sendSubmittedCaseData(final CaseDetails caseDetails) {
        if (isNotEmpty(caseDetails) && isNotEmpty(caseDetails.getData())) {
            CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

            try {
                RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData, caseDetails.getId());

                runVerificationsOnRoboticsData(roboticsData);

                EmailData emailData = prepareEmailData(roboticsData);

                emailService.sendEmail(roboticsEmailConfiguration.getSender(), emailData);

            } catch (Exception exc) {
                log.error("Robotics email notification failed for case with caseId {} and familyManNumber {} due to {}",
                    caseDetails.getId(), caseData.getFamilyManCaseNumber(), exc.getMessage());

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
}
