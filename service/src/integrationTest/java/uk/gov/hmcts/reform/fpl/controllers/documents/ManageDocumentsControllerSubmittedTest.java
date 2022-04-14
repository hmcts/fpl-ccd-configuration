package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    private static final String ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR = "furtherEvidenceDocumentsSolicitor";

    private static final String ANY_OTHER_DOCUMENTS_BUNDLE_NAME_ADMIN = "furtherEvidenceDocuments";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    ManageDocumentsControllerSubmittedTest() {
        super("manage-documents");
    }

    @BeforeEach
    void init() {
        givenFplService();
    }

    @Test
    void shouldNotPublishEventWhenUploadAnyDocumentNotificationFeatureIsDisabled() {
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialAnyOtherDocumentUploadedBySolicitor() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedBySolicitor() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialRespondentStatementUploadedBySolicitor() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedBySolicitor()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialAnyOtherDocumentUploadedByHmctsAdmin() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_ADMIN,
            true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedByHmctsAdmin() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_ADMIN,
            false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    @Test
    void shouldNotSendNotificationWhenConfidentialRespondentStatementUploadedByHmctsAdmin() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedByHmctsSAdmin()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }
}
