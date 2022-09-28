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

import java.beans.IntrospectionException;
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
    public static final String FROM_EMAIL = "noreply@reform.hmcts.net";

    private final CaseProgressionReportService caseProgressionReportService;
    private final EmailService emailService;
    private final CourtService courtService;

    @Async
    @EventListener
    public void notifyReport(CaseProgressionReportEvent event) {
        CaseData caseData = event.getCaseData();
        CaseProgressionReportEventData caseProgressionReportEventData = caseData.getCaseProgressionReportEventData();
        String courtCode = null;
        try {
            courtCode = caseProgressionReportService.getCourt(caseProgressionReportEventData);
        } catch (IntrospectionException e) {
            log.error("court lookup failed", e);
        }
        Optional<Court> court = courtService.getCourt(courtCode);
        String subject = String.join(" ",
                caseProgressionReportEventData.getReportType().getType(),
                "for court: ",
                court.map(Court::getName).orElse("Court not found")
        );

        try {
            Optional<File> fileReport = caseProgressionReportService.getFileReport(caseData);
            if (fileReport.isPresent()) {
                log.info("To notify subject: {}", subject);

                File file = fileReport.get();
                EmailAttachment attachment = EmailAttachment.document(
                        defaultIfNull(URLConnection.guessContentTypeFromName(file.getName()),
                                "application/octet-stream"),
                        Files.readAllBytes(file.toPath()),
                        String.join("-",
                                court.map(Court::getName)
                                    .map(name -> name.replace(" ",""))
                                    .orElse(""),
                                file.getName()));

                emailService.sendEmail(FROM_EMAIL,
                        EmailData.builder()
                                .recipient(event.getUserDetails().getEmail())
                                //TODO: comment out above for testing and uncomment below
                                //.recipient("somesh.acharya1@hmcts.net")
                                .subject(subject)
                                .attachments(Set.of(attachment))
                                .message(subject)
                                .build()
                );
                Files.delete(file.toPath());
                log.info("Notified cases with subject {} ", subject);
            } else {
                log.info("No records found for  subject {}", subject);
            }
        } catch (Exception e) {
            log.error("Notification exception for subject " + subject, e);
        }
    }
}
