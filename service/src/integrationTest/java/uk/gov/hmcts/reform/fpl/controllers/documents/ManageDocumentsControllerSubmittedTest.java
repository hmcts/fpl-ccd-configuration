package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    private static final String BUNDLE_NAME = "furtherEvidenceDocuments";

    @MockBean
    private NotificationClient notificationClient;

    ManageDocumentsControllerSubmittedTest() {
        super("manage-documents");
    }

    @Test
    void shouldNotPublishEventWhenConfidentialDocumentsAreUploaded() {
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldPublishNotificationEventWhenFurtherEvidenceIsUploaded() throws NotificationClientException {
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(EMAIL_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(EMAIL_REFERENCE));
    }
}
