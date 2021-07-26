package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.RegisteredRepresentativeSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.representative.UnregisteredRepresentativeSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
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
    private RegisteredRepresentativeSolicitorTemplate registeredTemplate;
    @Mock
    private UnregisteredRepresentativeSolicitorTemplate unregisteredTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private RegisteredRepresentativeSolicitorContentProvider registeredContentProvider;

    @Mock
    private UnregisteredRepresentativeSolicitorContentProvider unregisteredContentProvider;

    @InjectMocks
    private RespondentsSubmittedEventHandler underTest;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final Respondent respondent1 = Respondent.builder()
            .party(RespondentParty.builder().firstName("William").lastName("Smith").build())
            .solicitor(RespondentSolicitor.builder().email(recipient1).build())
            .build();

        final Respondent respondent2 = Respondent.builder()
            .party(RespondentParty.builder().firstName("Emma").lastName("Jones").build())
            .solicitor(RespondentSolicitor.builder().email(recipient2).build()).build();

        final CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(respondent1, respondent2))
            .build();

        final RespondentsSubmitted respondentsUpdated = new RespondentsSubmitted(caseData);

        when(respondentService.getRespondentsWithRegisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(respondent1, respondent2)));

        when(registeredContentProvider.buildContent(caseData, respondent1))
            .thenReturn(registeredTemplate);

        when(registeredContentProvider.buildContent(caseData, respondent2))
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

        final Respondent respondent = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().email(expectedEmail).build())
            .build();

        final CaseData caseData = CaseData.builder().respondents1(wrapElements(respondent)).build();

        final RespondentsSubmitted submittedCaseEvent = new RespondentsSubmitted(caseData);

        when(respondentService.getRespondentsWithUnregisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(respondent)));

        when(unregisteredContentProvider.buildContent(caseData, respondent))
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
