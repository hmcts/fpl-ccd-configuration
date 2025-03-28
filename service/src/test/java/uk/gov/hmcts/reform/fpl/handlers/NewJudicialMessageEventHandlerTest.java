package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageContentProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_ADDED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class NewJudicialMessageEventHandlerTest {
    private static final String CTSC_EMAIL = "ctsc@test.com";

    @Mock
    private NotificationService notificationService;

    @Mock
    private JudicialMessageContentProvider newJudicialMessageContentProvider;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @InjectMocks
    private NewJudicialMessageEventHandler newJudicialMessageEventHandler;

    @BeforeEach
    void setup() {
        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_EMAIL);
    }

    @Test
    void shouldNotifyJudicialMessageRecipientWhenNewJudicialMessageCreated() {
        String recipient = "David@fpla.com";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData();

        final NewJudicialMessageTemplate expectedParameters = NewJudicialMessageTemplate.builder().build();

        when(featureToggleService.isCourtNotificationEnabledForWa(any())).thenReturn(true);
        given(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .willReturn(expectedParameters);

        newJudicialMessageEventHandler.notifyJudicialMessageRecipient(
            new NewJudicialMessageEvent(caseData, judicialMessage)
        );

        verify(notificationService).sendEmail(
            JUDICIAL_MESSAGE_ADDED_TEMPLATE,
            recipient,
            expectedParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyJudicialMessageRecipientWhenToggledOff() {
        String recipient = "David@fpla.com";

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData().toBuilder()
            .court(Court.builder().name("test").code("000").build())
            .build();

        when(featureToggleService.isCourtNotificationEnabledForWa(any())).thenReturn(false);

        newJudicialMessageEventHandler.notifyJudicialMessageRecipient(
            new NewJudicialMessageEvent(caseData, judicialMessage)
        );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyCTSCWhenToggledOn() {
        String recipient = CTSC_EMAIL;

        JudicialMessage judicialMessage = JudicialMessage.builder()
            .sender("Paul@fpla.com")
            .recipient(recipient)
            .build();

        CaseData caseData = caseData().toBuilder()
            .court(Court.builder().name("test").code("000").build())
            .build();

        final NewJudicialMessageTemplate expectedParameters = NewJudicialMessageTemplate.builder().build();

        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);
        given(newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, judicialMessage))
            .willReturn(expectedParameters);
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);

        newJudicialMessageEventHandler.notifyJudicialMessageRecipient(
            new NewJudicialMessageEvent(caseData, judicialMessage)
        );

        verify(notificationService).sendEmail(
            JUDICIAL_MESSAGE_ADDED_TEMPLATE,
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

        given(featureToggleService.isCourtNotificationEnabledForWa(any())).willReturn(false);

        newJudicialMessageEventHandler.notifyJudicialMessageRecipient(
            new NewJudicialMessageEvent(caseData, judicialMessage)
        );

        verify(notificationService, never()).sendEmail(
            eq(JUDICIAL_MESSAGE_ADDED_TEMPLATE),
            eq(recipient),
            any(),
            eq(caseData.getId()));
    }

}
