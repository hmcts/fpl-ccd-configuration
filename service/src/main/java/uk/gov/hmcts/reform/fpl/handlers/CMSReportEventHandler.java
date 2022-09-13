package uk.gov.hmcts.reform.fpl.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CMSReportEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.event.CMSReportEventData;
import uk.gov.hmcts.reform.fpl.service.CMSReportService;
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
public class CMSReportEventHandler {
    private final CMSReportService cmsReportService;
    private final EmailService emailService;
    private final CourtService courtService;

    @Async
    @EventListener
    public void notifyReport(CMSReportEvent event) {
        CaseData caseData = event.getCaseData();
        CMSReportEventData cmsReportEventData = caseData.getCmsReportEventData();
        String courtCode = cmsReportService.getCourt(cmsReportEventData);
        Optional<Court> court = courtService.getCourt(courtCode);
        String subject = String.join(" ",
                cmsReportEventData.getReportType(),
                "for court: ",
                court.map(Court::getName).orElse("Court not found")
        );
        log.info("TO notify subject {}" , subject);
        //TODO: remove
        log.info("Notifying user {}", event.getUserDetails().getEmail());
        try {
            File fileReport = cmsReportService.getFileReport(caseData);
            EmailAttachment attachment = EmailAttachment.document(defaultIfNull(URLConnection.guessContentTypeFromName(fileReport.getName()),
                        "application/octet-stream"),
                Files.readAllBytes(fileReport.toPath()),
                fileReport.getName());

            emailService.sendEmail("noreply@reform.hmcts.net",
                EmailData.builder()
                    .recipient(event.getUserDetails().getEmail())
                    .subject(subject)
                    .attachments(Set.of(attachment))
                    .build()
            );
            log.info("Notified cases with subject {}" , subject);
        } catch (Exception e) {
            log.error("Notification exception for subject "+ subject, e);
        }

    }
}
