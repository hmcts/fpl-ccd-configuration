package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR;
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
    void shouldSendEmailToUnregisteredSolicitor() {
        final String expectedEmail = "test@test.com";
        final CaseData caseDataBefore = caseData();

        final CaseData caseData = CaseData.builder().respondents1(
            wrapElements(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(RespondentSolicitor.builder()
                    .email(expectedEmail)
                    .unregisteredOrganisation(UnregisteredOrganisation.builder().name("Unregistered Org Name").build())
                    .build()).build())
        ).build();

        final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
        final RespondentsUpdated submittedCaseEvent = new RespondentsUpdated(caseData, caseDataBefore);
        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(any(CaseData.class), any(
            RespondentSolicitor.class))).thenReturn(expectedTemplate);

        underTest.notifyUnregisteredSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            UNREGISTERED_RESPONDENT_SOLICICTOR,
            expectedEmail,
            expectedTemplate,
            caseData.getId());
    }

    @Test
    void shouldSendEmailToRegisteredSolicitors() {
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

        final CaseData caseDataBefore = caseData();
        final RespondentSolicitorTemplate expectedTemplate = RespondentSolicitorTemplate.builder().build();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            caseData, solicitor1)).thenReturn(expectedTemplate);

        when(respondentSolicitorContentProvider.buildRespondentSolicitorSubmissionNotification(
            caseData, solicitor2)).thenReturn(expectedTemplate);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
            recipient1,
            expectedTemplate,
            caseData.getId());

        verify(notificationService).sendEmail(
            REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
            recipient2,
            expectedTemplate,
            caseData.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"No"})
    void shouldNotSendEmailWhenLegalRepresentationIsNotSetToTheRespondent(String legalRepresentation) {
        final Respondent respondent = Respondent.builder().legalRepresentation(legalRepresentation).build();

        final CaseData caseData = caseData().toBuilder()
            .respondents1(wrapElements(respondent)).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @Test
    void shouldNotSendEmailWhenRespondentsWithSolicitorAreEmpty() {
        final Respondent respondentWithoutSolicitor = Respondent.builder().build();

        final CaseData caseData = caseData().toBuilder()
            .respondents1(wrapElements(respondentWithoutSolicitor)).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @Test
    void shouldNotSendEmailWhenRegisteredSolicitorsAreEmpty() {
        final Respondent respondentWithUnregisteredSolicitor = Respondent.builder().solicitor(
            RespondentSolicitor.builder()
                .unregisteredOrganisation(UnregisteredOrganisation.builder().name("org name").build()).build())
            .build();

        final CaseData caseData = caseData().toBuilder()
            .respondents1(wrapElements(respondentWithUnregisteredSolicitor)).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotSendEmailWhenRegisteredSolicitorsEmailsAreEmpty(String solicitorEmail) {
        final Respondent respondent1 = Respondent.builder()
            .solicitor(
                RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent1")
                    .email(solicitorEmail)
                    .organisation(Organisation.builder().organisationID("123").build())
                    .build())
            .build();

        final CaseData caseData = caseData().toBuilder().respondents1(wrapElements(respondent1)).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotSendEmailWhenRegisteredSolicitorsOrganisationIdIsEmpty(String organisationId) {
        final Respondent respondent = Respondent.builder()
            .solicitor(
                RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent1")
                    .email("test@test.com")
                    .organisation(Organisation.builder().organisationID(organisationId).build())
                    .build())
            .build();

        final CaseData caseData = caseData().toBuilder().respondents1(wrapElements(respondent)).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

    @Test
    void shouldNotSendEmailWhenNoRespondentsExist() {
        final CaseData caseData = caseData().toBuilder().respondents1(emptyList()).build();

        final CaseData caseDataBefore = caseData();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

        underTest.notifyRegisteredRespondentSolicitors(submittedCaseEvent);

        verifyNoInteractions(respondentSolicitorContentProvider);
    }

}
