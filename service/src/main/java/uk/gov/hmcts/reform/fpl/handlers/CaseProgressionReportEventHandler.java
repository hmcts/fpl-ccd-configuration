package uk.gov.hmcts.reform.fpl.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CaseProgressionReportEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseProgressionReportEventHandler {
    private final CaseProgressionReportService cmsReportService;
    private final EmailService emailService;
    private final CourtService courtService;

    @Async
    @EventListener
    public void notifyReport(CaseProgressionReportEvent event) {
        CaseData caseData = event.getCaseData();
        CaseProgressionReportEventData caseProgressionReportEventData = caseData.getCaseProgressionReportEventData();
        String courtCode = cmsReportService.getCourt(caseProgressionReportEventData);
        Optional<Court> court = courtService.getCourt(courtCode);
        String subject = String.join(" ",
                caseProgressionReportEventData.getReportType(),
                "for court: ",
                court.map(Court::getName).orElse("Court not found")
        );

        try {
            Optional<File> fileReport = cmsReportService.getFileReport(caseData);
            if (fileReport.isPresent()) {
                log.info("To notify subject: {}" , subject);

                File file = fileReport.get();
                EmailAttachment attachment = EmailAttachment.document(defaultIfNull(URLConnection.guessContentTypeFromName(file.getName()),
                                "application/octet-stream"),
                        Files.readAllBytes(file.toPath()),
                        String.join("-",
                                court.map(Court::getName)
                                    .map(name -> name.replace(" ",""))
                                    .orElse(""),
                                file.getName()));

                emailService.sendEmail("noreply@reform.hmcts.net",
                        EmailData.builder()
                                //TODO uncomment and remove next line.recipient(event.getUserDetails().getEmail())
                                .recipient("somesh.acharya1@hmcts.net")
                                .subject(subject)
                                .attachments(Set.of(attachment))
                                .message(subject)
                                .build()
                );
                log.info("Notified cases with subject {}", subject);
            } else {
                log.info("No records found for  subject {}" , subject);
            }
        } catch (Exception e) {
            log.error("Notification exception for subject "+ subject, e);
        }
    }
}
