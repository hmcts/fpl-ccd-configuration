package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.RegisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.UnregisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor.RegisteredRespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor.UnregisteredRespondentSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(MockitoExtension.class)
class RespondentsSubmittedEventHandlerTest {

    @Mock
    private RegisteredRespondentSolicitorTemplate registeredTemplate;
    @Mock
    private UnregisteredRespondentSolicitorTemplate unregisteredTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private RegisteredRespondentSolicitorContentProvider registeredContentProvider;

    @Mock
    private UnregisteredRespondentSolicitorContentProvider unregisteredContentProvider;

    @InjectMocks
    private RespondentsSubmittedEventHandler underTest;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final RespondentSolicitor solicitor1 = RespondentSolicitor.builder().email(recipient1).build();

        final RespondentSolicitor solicitor2 = RespondentSolicitor.builder().email(recipient2).build();

        final CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(mock(Respondent.class), mock(Respondent.class)))
            .build();

        final RespondentsSubmitted respondentsUpdated = new RespondentsSubmitted(caseData);

        when(respondentService.getRegisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(solicitor1, solicitor2)));

        when(registeredContentProvider.buildRespondentSolicitorSubmissionNotification(caseData, solicitor1))
            .thenReturn(registeredTemplate);

        when(registeredContentProvider.buildRespondentSolicitorSubmissionNotification(caseData, solicitor2))
            .thenReturn(registeredTemplate);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient1, registeredTemplate, caseData.getId()
        );

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient2, registeredTemplate, caseData.getId()
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";

        final RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder().email(expectedEmail).build();

        final CaseData caseData = CaseData.builder().respondents1(wrapElements(mock(Respondent.class))).build();

        final RespondentsSubmitted submittedCaseEvent = new RespondentsSubmitted(caseData);

        when(respondentService.getUnregisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(unregisteredSolicitor)));

        when(unregisteredContentProvider.buildContent(caseData))
            .thenReturn(unregisteredTemplate);

        underTest.notifyUnregisteredRespondentSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, expectedEmail, unregisteredTemplate, caseData.getId()
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = CaseData.builder().respondents1(emptyList()).build();

        final RespondentsSubmitted respondentsSubmitted = new RespondentsSubmitted(caseData);

        underTest.notifyRegisteredRespondentSolicitors(respondentsSubmitted);
        underTest.notifyUnregisteredRespondentSolicitors(respondentsSubmitted);

        verifyNoInteractions(registeredContentProvider);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(RespondentsSubmittedEventHandler.class).hasAsyncMethods(
            "notifyRegisteredRespondentSolicitors",
            "notifyUnregisteredRespondentSolicitors");
    }
}
