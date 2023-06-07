package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.COURT_BUNDLE_UPLOADED_NOTIFICATION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    private static final String ANY_OTHER_DOCUMENTS_BUNDLE_NAME = "furtherEvidenceDocumentsLA";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private FeatureToggleService featureToggleService;

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

    // Uploaded by Designated LA
    // Application Document
    @Test
    void shouldSendEmailsWhenApplicationDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingApplicationDocument());
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX,
                RESPONDENT_REP_1_EMAIL,
                RESPONDENT_SOLICITOR_1_EMAIL,
                CHILD_SOLICITOR_1_EMAIL,
                DEFAULT_CAFCASS_EMAIL
            ));
    }

    // Respondent Statement
    @Test
    void shouldSendEmailsWhenConfidentialRespondentStatementUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any Other Document
    @Test
    void shouldSendEmailsWhenConfidentialAnyOtherDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any other document (document relate to a hearing)
    @Test
    void shouldSendEmailsWhenConfidentialAnyOtherDocumentFromHearingsUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentFromHearingsUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Court Bundle
    @Test
    void shouldSendEmailsWhenCourtBundleUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());
        verifySendingNotificationToAllParties(notificationClient, COURT_BUNDLE_UPLOADED_NOTIFICATION, TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailToCafcassWhenNewCourtBundlePresentByDesignatedLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());

        verify(cafcassNotificationService).sendEmail(isA(CaseData.class),
            documentReferences.capture(),
            eq(COURT_BUNDLE),
            isA(CourtBundleData.class));

        Set<DocumentReference> value = documentReferences.getValue();
        DocumentReference documentReference = value.stream().findFirst().orElseThrow();
        assertThat(documentReference.getFilename()).isEqualTo("filename");
    }


    // Uploaded by Secondary LA
    // Application Document
    @Test
    void shouldSendEmailsWhenApplicationDocumentUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingApplicationDocument());
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX,
                RESPONDENT_REP_1_EMAIL,
                RESPONDENT_SOLICITOR_1_EMAIL,
                CHILD_SOLICITOR_1_EMAIL,
                DEFAULT_CAFCASS_EMAIL
            ));
    }

    @Test
    void shouldSendEmailToCafcassWhenNewCourtBundlePresent() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildCourtBundleData())
            .build();

        caseDetails.getData().put("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE);
        caseDetails.getData().put("caseLocalAuthorityName", LOCAL_AUTHORITY_1_NAME);

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        postSubmittedEvent(CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build());

        verify(cafcassNotificationService).sendEmail(isA(CaseData.class),
            documentReferences.capture(),
            eq(COURT_BUNDLE),
            isA(CourtBundleData.class));

        Set<DocumentReference> value = documentReferences.getValue();
        DocumentReference documentReference = value.stream().findFirst().orElseThrow();
        assertThat(documentReference.getFilename()).isEqualTo("filename");
    }

    // Respondent Statement
    @Test
    void shouldSendEmailsWhenConfidentialRespondentStatementUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialRespondentStatementUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingRespondentStatement(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any Other Document
    @Test
    void shouldSendEmailsWhenConfidentialAnyOtherDocumentUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingAnyOtherDocuments(ANY_OTHER_DOCUMENTS_BUNDLE_NAME, false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Any other document (document relate to a hearing)
    @Test
    void shouldSendEmailsWhenConfidentialAnyOtherDocumentFromHearingsUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(true));
        verifySendingNotification(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID, List.of(
                LOCAL_AUTHORITY_1_INBOX,
                LOCAL_AUTHORITY_2_INBOX
            ));
    }

    @Test
    void shouldSendEmailsWhenNonConfidentialAnyOtherDocumentFromHearingsUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        when(featureToggleService.isNewDocumentUploadNotificationEnabled()).thenReturn(true);
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingHearingFurtherEvidenceBundle(false));
        verifySendingNotificationToAllParties(notificationClient, DOCUMENT_UPLOADED_NOTIFICATION_TEMPLATE,
            TEST_CASE_ID);
    }

    // Court Bundle
    @Test
    void shouldSendEmailsWhenCourtBundleUploadedBySecondaryLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());
        verifySendingNotificationToAllParties(notificationClient, COURT_BUNDLE_UPLOADED_NOTIFICATION, TEST_CASE_ID);
    }

    @Test
    void shouldSendEmailToCafcassWhenNewCourtBundlePresentBySecondaryLA() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);
        postSubmittedEvent(buildCallbackRequestForAddingCourtBundle());

        verify(cafcassNotificationService, timeout(1000)).sendEmail(isA(CaseData.class),
            documentReferences.capture(),
            eq(COURT_BUNDLE),
            isA(CourtBundleData.class));

        Set<DocumentReference> value = documentReferences.getValue();
        DocumentReference documentReference = value.stream().findFirst().orElseThrow();
        assertThat(documentReference.getFilename()).isEqualTo("filename");
    }

    private Map<String, Object> buildCourtBundleData() {
        return new HashMap<>(Map.of(
            "courtBundleListV2",
            wrapElements(HearingCourtBundle.builder()
                .hearing("hearing")
                .courtBundle(wrapElements(CourtBundle.builder()
                    .document(getPDFDocument())
                    .hearing("hearing")
                    .dateTimeUploaded(LocalDateTime.now())
                    .uploadedBy("LA")
                    .build()))
                .build())));
    }

    private DocumentReference getPDFDocument() {
        return DocumentReference.builder()
            .filename("filename")
            .url(randomAlphanumeric(10))
            .binaryUrl(randomAlphanumeric(10))
            .build();
    }
}
