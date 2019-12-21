package uk.gov.hmcts.reform.fpl.service.robotics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseSubmittedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.robotics.RoboticsData;
import uk.gov.hmcts.reform.fpl.service.EmailService;

import static java.util.Set.of;
import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.json;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoboticsNotificationService {
    private final EmailService emailService;
    private final RoboticsDataService roboticsDataService;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;

    @EventListener
    public void notifyRoboticsOfSubmittedCaseData(final CaseSubmittedEvent event) {
        EmailData emailData = prepareEmailData(event.getCaseData());
        emailService.sendEmail(roboticsEmailConfiguration.getSender(), emailData);
    }

    private EmailData prepareEmailData(final CaseData caseData) {
        RoboticsData roboticsData = roboticsDataService.prepareRoboticsData(caseData);

        final String fileNamePrefix = "CaseSubmitted_";
        final String fileName = join(fileNamePrefix, roboticsData.getCaseNumber());
        final String fileNameAndExtension = join(fileName, ".json");

        final String roboticsJsonData = roboticsDataService.convertRoboticsDataToJson(roboticsData);

        return EmailData.builder()
            .subject(fileName)
            .to(roboticsEmailConfiguration.getRecipient())
            .attachments(of(json(roboticsJsonData.getBytes(), fileNameAndExtension)))
            .build();
    }
}
