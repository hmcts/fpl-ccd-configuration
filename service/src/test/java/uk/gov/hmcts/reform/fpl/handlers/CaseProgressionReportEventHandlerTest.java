package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.CaseProgressionReportEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.email.EmailAttachment;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseProgressionReportType.MISSING_TIMETABLE;
import static uk.gov.hmcts.reform.fpl.handlers.CaseProgressionReportEventHandler.FROM_EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CsvWriter.ATTRIBUTE;

@ExtendWith(MockitoExtension.class)
class CaseProgressionReportEventHandlerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private CourtService courtService;

    @Mock
    private CaseProgressionReportService caseProgressionReportService;

    @InjectMocks
    private CaseProgressionReportEventHandler caseProgressionReportEventHandler;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    @Test
    void shouldSendNotificationWhenReportFilePresent() throws IOException {

        String courtId = "344";
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts(courtId)
                .reportType(MISSING_TIMETABLE)
                .build();
        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        String toEmail = "test@gmail.com";
        CaseProgressionReportEvent caseProgressionReportEvent = new CaseProgressionReportEvent(
                caseDataSelected,
                UserDetails.builder().email(toEmail).build());

        Path path = Files.createTempFile("CaseProgressionReport", ".xlsx", ATTRIBUTE);
        byte[] actualContent = Files.readAllBytes(path);
        File file = path.toFile();
        when(caseProgressionReportService.getCourt(caseProgressionReportEventData))
                .thenReturn(courtId);
        when(courtService.getCourt(courtId))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));
        when(caseProgressionReportService.getFileReport(caseDataSelected))
                .thenReturn(Optional.of(file));

        caseProgressionReportEventHandler.notifyReport(caseProgressionReportEvent);
        verify(emailService).sendEmail(eq(FROM_EMAIL), emailDataArgumentCaptor.capture());
        EmailData emailData = emailDataArgumentCaptor.getValue();
        assertThat(emailData.getRecipient()).isEqualTo(toEmail);
        assertThat(emailData.getSubject()).isEqualTo("Missing timetable for court:  Family court Swansea");
        assertThat(emailData.getMessage()).isEqualTo("Missing timetable for court:  Family court Swansea");

        Set<EmailAttachment> attachments = emailData.getAttachments();
        assertThat(attachments).isNotEmpty();
        Optional<EmailAttachment> emailAttachment = attachments.stream().findFirst();
        assertThat(emailAttachment).isPresent();
        assertThat(emailAttachment.get().getData().getInputStream().readAllBytes())
                .isEqualTo(actualContent);
        assertThat(emailAttachment.get().getFilename()).contains("FamilycourtSwansea-CaseProgressionReport");
        assertThat(file.exists()).isFalse();
    }

    @Test
    void shouldNotSendNotificationWhenNoReportFilePresent()  {

        String courtId = "344";
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts(courtId)
                .reportType(MISSING_TIMETABLE)
                .build();
        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        String toEmail = "test@gmail.com";
        CaseProgressionReportEvent caseProgressionReportEvent = new CaseProgressionReportEvent(
                caseDataSelected,
                UserDetails.builder().email(toEmail).build());


        when(caseProgressionReportService.getCourt(caseProgressionReportEventData))
                .thenReturn(courtId);
        when(courtService.getCourt(courtId))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));
        when(caseProgressionReportService.getFileReport(caseDataSelected))
                .thenReturn(Optional.empty());

        caseProgressionReportEventHandler.notifyReport(caseProgressionReportEvent);
        verify(emailService ,never()).sendEmail(eq(FROM_EMAIL), isA(EmailData.class));
    }
}