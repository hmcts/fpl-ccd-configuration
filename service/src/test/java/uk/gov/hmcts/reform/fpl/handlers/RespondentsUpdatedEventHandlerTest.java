package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class RespondentsUpdatedEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RespondentSolicitorContentProvider respondentSolicitorContentProvider;

    @InjectMocks
    private RespondentsUpdatedEventHandler underTest;

    @Test
    void shouldOnlySendEmailToUpdatedRegisteredSolicitors() {
        final String recipient1 = "solicitor1@test.com";
        final String recipient2 = "solicitor2@test.com";

        final RespondentSolicitor solicitor1 = RespondentSolicitor.builder()
            .email(recipient1)
            .firstName("First")
            .lastName("Respondent")
            .organisation(Organisation.builder().organisationID("111").organisationName("org1").build())
            .build();

        final RespondentSolicitor solicitor2 = RespondentSolicitor.builder()
            .email(recipient2)
            .firstName("Second")
            .lastName("Respondent")
            .organisation(Organisation.builder().organisationID("222").organisationName("org2").build())
            .build();

        final CaseData caseData = caseData().toBuilder()
            .respondents1(wrapElements(
                Respondent.builder().legalRepresentation("Yes").solicitor(solicitor1).build(),
                Respondent.builder().legalRepresentation("Yes").solicitor(solicitor2).build()))
            .build();

        final CaseData caseDataBefore = caseData().toBuilder()
            .respondents1(wrapElements(
                Respondent.builder().legalRepresentation("Yes").solicitor(solicitor1).build(),
                Respondent.builder().legalRepresentation("No").build()))
            .build();

        final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            caseData, solicitor2)).thenReturn(expectedTemplate);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
            recipient2,
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";
        final CaseData caseDataBefore = caseData();

        final CaseData caseData = CaseData.builder().respondents1(
            wrapElements(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder()
                    .email(expectedEmail)
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
                    .build()).build()))
            .build();

        final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
        final RespondentsUpdated submittedCaseEvent = new RespondentsUpdated(caseData, caseDataBefore);
        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(any(CaseData.class), any(
            RespondentSolicitor.class))).thenReturn(expectedTemplate);

        underTest.notifyUnregisteredSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = caseData().toBuilder().respondents1(emptyList()).build();

        final CaseData caseDataBefore = caseData();
        final RespondentsUpdated respondentsUpdated = new RespondentsUpdated(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(respondentsUpdated);
        underTest.notifyUnregisteredSolicitors(respondentsUpdated);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

}
