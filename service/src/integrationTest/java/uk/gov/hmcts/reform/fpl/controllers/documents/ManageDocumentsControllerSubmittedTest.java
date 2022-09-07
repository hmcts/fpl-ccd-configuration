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
import static org.mockito.BDDMockito.given;
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
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(false);
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifyNoInteractions(notificationClient);
    }

    // Uploaded by solicitor
    // Respondent Statement
    @Test
    void shouldSendEmailsWhenRespondentStatementUploadedBySolicitor()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any other document
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentUploadedBySolicitor() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Same behaviour as solicitior for any document but with LA barrister user
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentUploadedByLaBarrister() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.LABARRISTER);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Same behaviour as solicitior for any document but with barrister user
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentUploadedByBarrister() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.BARRISTER);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_SOLICITOR,
            false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any other document (document relate to a hearing)
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentFromHearingsUploadedBySolicitor()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.SOLICITORA);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Check above test with la barrister as behaviour will be identical
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentFromHearingsUploadedByLaBarrister()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.LABARRISTER);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Same as above but for barrister as behaviour will be identical
    @Test
    void shouldSendEmailsWhenAnyOtherDocumentFromHearingsUploadedByBarrister()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, CaseRole.BARRISTER);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Uploaded by HMCTS Admin
    // Respondent Statement
    @Test
    void shouldNotSendNotificationWhenConfidentialRespondentStatementUploadedByHmctsAdmin() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(true, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedByHmctsAdmin()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false,true));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any Other Document
    @Test
    void shouldNotSendNotificationWhenConfidentialAnyOtherDocumentUploadedByHmctsAdmin() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_ADMIN,
            true, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedByHmctsAdmin() throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME_ADMIN,
            false, true));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any other document (document relate to a hearing)
    @Test
    void shouldNotSendEmailsWhenConfidentialEvidenceBundleFromHearingsUploadedByHmctsAdmin() {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(true, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenConfidentialEvidenceBundleFromHearingsUploadedByHmctsAdmin()
        throws NotificationClientException {
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false, true));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }
}
