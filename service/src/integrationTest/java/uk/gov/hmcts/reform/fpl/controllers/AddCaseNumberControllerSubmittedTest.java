package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class AddCaseNumberControllerSubmittedTest extends AbstractCallbackTest {

    AddCaseNumberControllerSubmittedTest() {
        super("add-case-number");
    }

    @Captor
    private ArgumentCaptor<EmailData> email;

    @MockBean
    private EmailService emailService;

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
    }

    @Test
    void shouldNotSendNotificationToRoboticsWhenCaseNumberUpdated() {
        CaseDetails caseDetailsBefore = buildCaseWithNumber("CASE1");
        CaseDetails caseDetails = buildCaseWithNumber("CASE2");
        CallbackRequest event = buildEvent(caseDetailsBefore, caseDetails);

        postSubmittedEvent(event);

        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(emailService, never()).sendEmail(any(), any());
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
