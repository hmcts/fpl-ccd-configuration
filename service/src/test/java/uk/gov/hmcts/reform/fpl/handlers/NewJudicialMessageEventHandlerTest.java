package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewJudicialMessageContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NewJudicialMessageEventHandler.class})
class NewJudicialMessageEventHandlerTest {
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NewJudicialMessageContentProvider newJudicialMessageContentProvider;

    @Autowired
    private NewJudicialMessageEventHandler newJudicialMessageEventHandler;

    @Test
    void shouldNotifyJudicialMessageRecipientWhenNewJudicialMessageCreated() {
        String recipient = "David@fpla.com";

        CaseData caseData = caseData().toBuilder()
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .sender("Paul@fpla.com")
                .recipient(recipient)
                .build())
            .build();

        final NewJudicialMessageTemplate expectedParameters = NewJudicialMessageTemplate.builder().build();

        given(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData))
            .willReturn(expectedParameters);

        newJudicialMessageEventHandler.notifyJudicialMessageRecipient(new NewJudicialMessageEvent(caseData));

        verify(notificationService).sendEmail(
            NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE,
            recipient,
            expectedParameters,
            caseData.getId());
    }
}
