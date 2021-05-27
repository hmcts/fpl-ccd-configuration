package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
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
class RespondentsUpdatedEventHandlerTest {

    private static final long CASE_ID = 123L;

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
    private RespondentsUpdatedEventHandler underTest;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final RespondentSolicitor solicitor1 = RespondentSolicitor.builder().email(recipient1).build();
        final RespondentSolicitor solicitor2 = RespondentSolicitor.builder().email(recipient2).build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(mock(Respondent.class), mock(Respondent.class)))
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .respondents1(wrapElements(mock(Respondent.class)))
            .build();

        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        when(respondentService.getRegisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(solicitor1, solicitor2)));

        when(respondentService.getRegisteredSolicitors(caseDataBefore.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(solicitor1)));

        when(registeredContentProvider.buildRespondentSolicitorSubmissionNotification(caseData, solicitor2))
            .thenReturn(registeredTemplate);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, recipient2, registeredTemplate, CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";
        final CaseData caseDataBefore = CaseData.builder().build();

        final RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder().email(expectedEmail).build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(mock(Respondent.class)))
            .build();

        final RespondentsUpdated submittedCaseEvent = new RespondentsUpdated(caseData, caseDataBefore);

        when(respondentService.getUnregisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(unregisteredSolicitor)));

        when(unregisteredContentProvider.buildContent(caseData)).thenReturn(unregisteredTemplate);

        underTest.notifyUnregisteredRespondentSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE, expectedEmail, unregisteredTemplate, CASE_ID
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = CaseData.builder().respondents1(emptyList()).build();

        final CaseData caseDataBefore = CaseData.builder().build();
        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);
        underTest.notifyUnregisteredRespondentSolicitors(respondentsUpdated);

        verifyNoInteractions(registeredContentProvider);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(RespondentsUpdatedEventHandler.class).hasAsyncMethods(
            "notifyRegisteredRespondentSolicitors", "notifyUnregisteredRespondentSolicitors"
        );
    }
}
