package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ActiveProfiles("integration-test")
@WebMvcTest(ReturnApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationSubmittedTest extends AbstractControllerTest {
    private static final String NOTIFICATION_REFERENCE = "localhost/" + 12345;

    @MockBean
    private NotificationClient notificationClient;

    ReturnApplicationSubmittedTest() {
        super("return-application");
    }

    @Test
    void shouldNotifyTheLocalAuthorityWhenCaseReturned() throws Exception {
        postSubmittedEvent(populatedCaseDetails());

        verify(notificationClient).sendEmail(
            eq(APPLICATION_RETURNED_TO_THE_LA), eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(), eq(NOTIFICATION_REFERENCE));
    }
}
