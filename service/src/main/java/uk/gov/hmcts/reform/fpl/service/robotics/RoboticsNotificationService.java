package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseNumberAdded;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import static java.util.Set.of;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsDataVerificationHelper.runVerificationsOnRoboticsData;
import static uk.gov.hmcts.reform.fpl.utils.RoboticsDataVerificationHelper.verifyRoboticsJsonData;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsNotificationService {
    private final EmailService emailService;
    private final RoboticsDataService roboticsDataService;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;

    @EventListener
    public void notifyRoboticsOfSubmittedCaseData(final CaseNumberAdded event) {
        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(event.getCaseData());

        EmailData emailData = prepareEmailData(roboticsData);
        emailService.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(final RoboticsData roboticsData) {
        final String roboticsJsonData = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        runVerificationsOnRoboticsData(roboticsData);
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
