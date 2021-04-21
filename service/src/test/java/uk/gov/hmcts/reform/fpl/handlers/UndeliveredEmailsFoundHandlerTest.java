package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.UndeliveredEmailsFound;
import uk.gov.hmcts.reform.fpl.model.UndeliveredEmail;
import uk.gov.hmcts.reform.fpl.model.notify.UndeliveredEmailsNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.UndeliveredEmailsContentProvider;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNDELIVERED_EMAILS_TEMPLATE;

@ExtendWith(MockitoExtension.class)
class UndeliveredEmailsFoundHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UndeliveredEmailsContentProvider contentProvider;

    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @InjectMocks
    private UndeliveredEmailsFoundHandler underTest;

    @Test
    void shouldSendUndeliveredEmailsReportToCtsc() {
        final String ctscEmail = "ctsc@test.com";

        final List<UndeliveredEmail> undeliveredEmails = List.of(UndeliveredEmail.builder()
            .recipient("test@test.com")
            .build());

        final UndeliveredEmailsNotifyData undeliveredEmailsNotifyData = UndeliveredEmailsNotifyData.builder()
            .emails("To: test@test.com")
            .build();

        final UndeliveredEmailsFound event = new UndeliveredEmailsFound(undeliveredEmails);

        given(ctscEmailLookupConfiguration.getEmail()).willReturn(ctscEmail);
        given(contentProvider.buildParameters(undeliveredEmails)).willReturn(undeliveredEmailsNotifyData);

        underTest.sendUndeliveredEmailsReport(event);

        verify(contentProvider).buildParameters(undeliveredEmails);
        verify(notificationService)
            .sendEmail(UNDELIVERED_EMAILS_TEMPLATE, ctscEmail, undeliveredEmailsNotifyData, "undeliveredEmails");

        verifyNoMoreInteractions(notificationService);
    }
}
