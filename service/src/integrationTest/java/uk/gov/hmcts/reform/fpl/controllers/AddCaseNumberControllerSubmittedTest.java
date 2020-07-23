package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ActiveProfiles("integration-test")
@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class AddCaseNumberControllerSubmittedTest extends AbstractControllerTest {

    AddCaseNumberControllerSubmittedTest() {
        super("add-case-number");
    }

    @Captor
    private ArgumentCaptor<EmailData> email;

    @MockBean
    private EmailService emailService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Spy
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void shouldSendNotificationToRoboticsWhenCaseNumberAdded() {
        CaseDetails caseDetailsBefore = buildCaseWithNumber("");
        CaseDetails caseDetails = buildCaseWithNumber("CASE1");
        CallbackRequest event = buildEvent(caseDetailsBefore, caseDetails);

        postSubmittedEvent(event);

        verify(emailService).sendEmail(eq("sender@example.com"), email.capture());
        assertThat(email.getValue().getRecipient()).isEqualTo("FamilyPublicLaw+robotics-test@gmail.com");
        verifyTaskListUpdated(caseDetails);
    }

    @Test
    void shouldNotSendNotificationToRoboticsWhenCaseNumberUpdated() {
        CaseDetails caseDetailsBefore = buildCaseWithNumber("CASE1");
        CaseDetails caseDetails = buildCaseWithNumber("CASE2");
        CallbackRequest event = buildEvent(caseDetailsBefore, caseDetails);

        postSubmittedEvent(event);

        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(emailService, never()).sendEmail(any(), any());
        verifyTaskListUpdated(caseDetails);
    }

    private void verifyTaskListUpdated(CaseDetails caseDetails) {
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseDetails.getId()),
            eq("internal-update-case-info"),
            anyMap());
    }

    private static CaseDetails buildCaseWithNumber(String caseNumber) {
        CaseDetails caseDetails = populatedCaseDetails();
        caseDetails.getData().put("familyManCaseNumber", caseNumber);
        return caseDetails;
    }

    private static CallbackRequest buildEvent(CaseDetails caseDetailsBefore, CaseDetails caseDetailsAfter) {
        return CallbackRequest.builder()
            .caseDetails(caseDetailsAfter)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }
}
