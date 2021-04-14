package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.RespondentsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
}
