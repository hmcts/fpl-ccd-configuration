package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;

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
    private static final String SOLICITOR_BUNDLE_NAME = "furtherEvidenceDocumentsSolicitor";

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    ManageDocumentsControllerSubmittedTest() {
        super("manage-documents");
    }

    @Test
    void shouldNotPublishEventWhenUploadNotificationFeatureIsDisabled() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(false);
        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, false));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldNotPublishEventWhenConfidentialDocumentsAreUploaded() {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any(), any()))
            .thenReturn(buildCaseAssignedUserRole("[LASOLICITOR]"));

        postSubmittedEvent(buildCallbackRequest(BUNDLE_NAME, true));
        verifyNoInteractions(notificationClient);
    }

    @Test
    void shouldPublishEventWhenUploadNotificationFeatureIsEnabled() throws NotificationClientException {
        when(featureToggleService.isFurtherEvidenceUploadNotificationEnabled()).thenReturn(true);
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(caseAccessDataStoreApi.getUserRoles(any(), any(), any(), any()))
            .thenReturn(buildCaseAssignedUserRole("[SOLICITORA]"));

        postSubmittedEvent(buildCallbackRequest(SOLICITOR_BUNDLE_NAME, false));

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

    private CaseAssignedUserRolesResource buildCaseAssignedUserRole(String role) {
        return CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(List.of(
            CaseAssignedUserRole.builder()
                .caseRole(role)
                .userId("USER_1_ID")
                .caseDataId("123")
                .build()))
            .build();
    }
}
