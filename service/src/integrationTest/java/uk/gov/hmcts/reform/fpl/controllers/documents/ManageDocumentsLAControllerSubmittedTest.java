package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerSubmittedTest extends ManageDocumentsControllerSubmittedBaseTest {

    private static final String BUNDLE_NAME = "furtherEvidenceDocumentsLA";

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationClient notificationClient;

    ManageDocumentsLAControllerSubmittedTest() {
        super("manage-documents-la");
    }

    @BeforeEach
    void init() {
        givenFplService();
    }

    @Test
    void shouldNotPublishEventWhenUploadNotificationFeatureIsDisabled() {
        given(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).willReturn(false);

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotPublishEventWhenConfidentialDocumentsAreUploaded() {
        given(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).willReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, true));

        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldSendEmailsWhenNotificationEnabledAndDocumentUploadedByDesignatedLA() throws NotificationClientException {
        given(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).willReturn(true);
        given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder().build());

        givenCaseRoles(TEST_CASE_ID, USER_ID, LASOLICITOR);

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(REP_1_EMAIL),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));

        verify(notificationClient, never()).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_1_INBOX),
            anyMap(),
            any());

        verify(notificationClient, never()).sendEmail(
            any(),
            eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(),
            any());
    }

    @Test
    void shouldSendEmailsWhenNotificationEnabledAndDocumentUploadedBySecondaryLA() throws NotificationClientException {
        given(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).willReturn(true);
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

        verify(notificationClient).sendEmail(
            eq(FURTHER_EVIDENCE_UPLOADED_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_2_INBOX),
            anyMap(),
            eq(notificationReference(TEST_CASE_ID)));
    }
}
