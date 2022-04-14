package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_BUNDLE_UPLOADED_NOTIFICATION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    private static final String ANY_OTHER_DOCUMENTS_BUNDLE_NAME = "furtherEvidenceDocumentsLA";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    @Captor
    private ArgumentCaptor<Set<DocumentReference>> documentReferences;

    ManageDocumentsLAControllerSubmittedTest() {
        super("manage-documents-la");
    }

    @BeforeEach
    void init() {
        givenFplService();
    }

    @Test
    void shouldNotSendEmailsWhenConfidentialApplicationDocumentUploadedByDesignatedLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingApplicationDocument(true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenApplicationDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingApplicationDocument(false));
        verifySendingNotificationToAllParties(notificationClient, FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    @Test
    void shouldNotSendEmailsWhenConfidentialAnyOtherDocumentUploadedByDesignatedLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, false));
        verifySendingNotificationToAllParties(notificationClient, FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE
            , TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, false));
        verifySendingNotificationToAllParties(notificationClient, FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailsWhenConfidentialAnyOtherDocumentUploadedBySecondaryLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenCourtBundleUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());
        verifySendingNotificationToAllParties(notificationClient, COURT_BUNDLE_UPLOADED_NOTIFICATION, TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailToCafcassWhenNewCourtBundlePresent() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());

        verify(cafcassNotificationService).sendEmail(isA(CaseData.class),
            documentReferences.capture(),
            eq(COURT_BUNDLE),
            isA(CourtBundleData.class));

        Set<DocumentReference> value = documentReferences.getValue();
        DocumentReference documentReference = value.stream().findFirst().orElseThrow();
        assertThat(documentReference.getFilename()).isEqualTo("filename");
    }

    @Test
    void shouldNotSendEmailsWhenConfidentialRespondentStatementUploadedByDesignatedLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement( true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement( false));
        verifySendingNotificationToAllParties(notificationClient, FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE
            , TEST_CASE_ID);
    }
}
