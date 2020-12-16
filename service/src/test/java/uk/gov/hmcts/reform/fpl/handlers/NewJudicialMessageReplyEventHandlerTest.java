package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageReplyContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class NewJudicialMessageReplyEventHandlerTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private JudicialMessageReplyContentProvider newJudicialMessageReplyContentProvider;

    @InjectMocks
    private NewJudicialMessageReplyEventHandler newJudicialMessageReplyEventHandler;

    @Test
    void shouldNotifyJudicialMessageRecipientWhenNewJudicialMessageReplyCreated() {
        String recipient = "David@fpla.com";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData();

        final NewJudicialMessageReplyTemplate expectedParameters = NewJudicialMessageReplyTemplate.builder().build();

        given(newJudicialMessageReplyContentProvider.buildNewJudicialMessageReplyTemplate(caseData, judicialMessage))
            .willReturn(expectedParameters);

        newJudicialMessageReplyEventHandler.notifyJudicialMessageRecipientOfReply(
            new NewJudicialMessageReplyEvent(caseData, judicialMessage)
        );

        verify(notificationService).sendEmail(
            JUDICIAL_MESSAGE_REPLY_TEMPLATE,
            recipient,
            expectedParameters,
            caseData.getId());
    }
}
