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
import uk.gov.hmcts.reform.fpl.model.cafcass.CourtBundleData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {
    private static final String APPLICATION_DOCUMENT_BUNDLE_NAME = "applicationDocuments";
    private static final String BUNDLE_NAME = "furtherEvidenceDocumentsLA";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    @Captor
    private ArgumentCaptor<Set<DocumentReference>> documentReferences;

    @Captor
    private ArgumentCaptor<CourtBundleData> courtBundleCaptor;

    ManageDocumentsLAControllerSubmittedTest() {
        super("manage-documents-la");
    }

    @BeforeEach
    void init() {
        givenFplService();
    }

    @Test
    void shouldNotPublishEventWhenConfidentialDocumentsAreUploaded() {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, true));

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotSendEmailsWhenNotificationEnabledAndConfidentialApplicationDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());
        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);

        postSubmittedEvent(buildCallbackRequestWithApplicationDocument(APPLICATION_DOCUMENT_BUNDLE_NAME, true));

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNotificationEnabledAndApplicationDocumentUploadedByDesignatedLA()
        throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);

        postSubmittedEvent(buildCallbackRequestWithApplicationDocument(APPLICATION_DOCUMENT_BUNDLE_NAME, false));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));

        verify(notificationClient).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            any());

        verify(notificationClient).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(),
            any());
    }

    @Test
    void shouldSendEmailsWhenNotificationEnabledAndDocumentUploadedByDesignatedLA() throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));

        verify(notificationClient).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            any());

        verify(notificationClient).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(),
            any());
    }

    @Test
    void shouldSendEmailsWhenNotificationEnabledAndDocumentUploadedBySecondaryLA() throws NotificationClientException {
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        givenCaseRoles(TEST_CASE_ID, USER_ID, LASHARED);

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));
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

    private Map<String, Object> buildCourtBundleData() {
        return new HashMap<>(Map.of(
        "courtBundleList", wrapElements(
                CourtBundle.builder()
                    .document(getPDFDocument())
                    .hearing("hearing")
                    .dateTimeUploaded(LocalDateTime.now())
                    .uploadedBy("LA")
                    .build()
                )
            ));
    }

    private DocumentReference getPDFDocument() {
        return DocumentReference.builder()
            .filename("filename")
            .url(randomAlphanumeric(10))
            .binaryUrl(randomAlphanumeric(10))
            .build();

    }
}
