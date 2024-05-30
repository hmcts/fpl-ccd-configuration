package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.JudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.JudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageReplyContentProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class JudicialMessageReplyEventHandlerTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private JudicialMessageReplyContentProvider judicialMessageReplyContentProvider;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private JudicialMessageReplyEventHandler judicialMessageReplyEventHandler;

    @Test
    void shouldNotifyJudicialMessageRecipientWhenJudicialMessageReplyCreated() {
        String recipient = "David@fpla.com";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData();

        final JudicialMessageReplyTemplate expectedParameters = JudicialMessageReplyTemplate.builder().build();

        given(judicialMessageReplyContentProvider.buildJudicialMessageReplyTemplate(caseData, judicialMessage))
            .willReturn(expectedParameters);
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);

        judicialMessageReplyEventHandler.notifyRecipientOfReply(
            new JudicialMessageReplyEvent(caseData, judicialMessage)
        );

        verify(notificationService).sendEmail(
            JUDICIAL_MESSAGE_REPLY_TEMPLATE,
            recipient,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotSendEmailIfWAEmailsAreDisabled() {
        String recipient = "David@fpla.com";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData();

        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(false);

        judicialMessageReplyEventHandler.notifyRecipientOfReply(
            new JudicialMessageReplyEvent(caseData, judicialMessage)
        );

        verify(notificationService, never()).sendEmail(
            eq(JUDICIAL_MESSAGE_REPLY_TEMPLATE),
            eq(recipient),
            any(),
            eq(caseData.getId()));
    }
}
