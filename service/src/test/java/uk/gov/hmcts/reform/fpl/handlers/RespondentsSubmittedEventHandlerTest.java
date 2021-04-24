package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.RespondentsSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(MockitoExtension.class)
class RespondentsSubmittedEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RespondentService respondentService;

    @Mock
    private RespondentSolicitorContentProvider respondentSolicitorContentProvider;

    @InjectMocks
    private RespondentsSubmittedEventHandler underTest;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
            .email(recipient1)
            .organisation(Organisation.builder().organisationID("111").organisationName("org1").build())
            .build();

        final RespondentSolicitor solicitor2 = RespondentSolicitor.builder()
            .email(recipient2)
            .organisation(Organisation.builder().organisationID("222").organisationName("org2").build())
            .build();

        final CaseData caseData = CaseData.builder()
            .id(123L)
            .respondents1(wrapElements(
                Respondent.builder().legalRepresentation("Yes").solicitor(solicitor1).build(),
                Respondent.builder().legalRepresentation("Yes").solicitor(solicitor2).build()))
            .build();


        final RespondentSolicitorTemplate expectedTemplate = mock(RespondentSolicitorTemplate.class);
        final RespondentsSubmitted respondentsUpdated = new RespondentsSubmitted(caseData);

        when(respondentService.getRegisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(solicitor1, solicitor2)));

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            caseData, solicitor1)).thenReturn(expectedTemplate);

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            caseData, solicitor2)).thenReturn(expectedTemplate);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
            recipient1,
            expectedTemplate,
            caseData.getId());

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
            recipient2,
            expectedTemplate,
            caseData.getId());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";

        final RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder()
            .email(expectedEmail)
            .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
            .build();

        final CaseData caseData = CaseData.builder().respondents1(
            wrapElements(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(unregisteredSolicitor).build()))
            .build();

        final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
        final RespondentsSubmitted submittedCaseEvent = new RespondentsSubmitted(caseData);

        when(respondentService.getUnregisteredSolicitors(caseData.getRespondents1()))
            .thenReturn(new ArrayList<>(List.of(unregisteredSolicitor)));

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            any(CaseData.class), any(RespondentSolicitor.class))).thenReturn(expectedTemplate);

        underTest.notifyUnregisteredSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICITOR_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = CaseData.builder().respondents1(emptyList()).build();

        final RespondentsSubmitted respondentsSubmitted = new RespondentsSubmitted(caseData);

        underTest.notifyRegisteredRespondentSolicitors(respondentsSubmitted);
        underTest.notifyUnregisteredSolicitors(respondentsSubmitted);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(RespondentsUpdatedEventHandler.class).hasAsyncMethods(
            "notifyRegisteredRespondentSolicitors",
            "notifyUnregisteredSolicitors");
    }
}
