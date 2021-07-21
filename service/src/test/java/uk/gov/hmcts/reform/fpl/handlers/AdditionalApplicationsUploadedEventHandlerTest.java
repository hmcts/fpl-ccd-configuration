package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AdditionalApplicationsUploadedEventHandler.class, CourtService.class})
class AdditionalApplicationsUploadedEventHandlerTest {
    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private CourtService courtService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @Autowired
    private AdditionalApplicationsUploadedEventHandler additionalApplicationsUploadedEventHandler;

    private AdditionalApplicationsUploadedTemplate additionalApplicationsParameters =
        getAdditionalApplicationsUploadedTemplateParameters();

    @BeforeEach
    void before() {
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
    }

    @Test
    void shouldNotifyHmctsAdminOnAdditionalApplicationsUploadedByNonHmctsAdmin() {
        CaseData caseData = caseData();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(courtService.getCourtEmail(caseData))
            .willReturn("hmcts-admin@test.com", COURT_CODE);

        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData))
            .willReturn(additionalApplicationsParameters);

        additionalApplicationsUploadedEventHandler.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE,
            "hmcts-admin@test.com",
            additionalApplicationsParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyHmctsAdminOnAdditionalApplicationsUploadByHmctsAdmin() {
        CaseData caseData = caseData();

        given(requestData.userRoles()).willReturn(new HashSet<>(Arrays.asList("caseworker", "caseworker-publiclaw",
            "caseworker-publiclaw-courtadmin")));

        additionalApplicationsUploadedEventHandler.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    private AdditionalApplicationsUploadedTemplate getAdditionalApplicationsUploadedTemplateParameters() {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return AdditionalApplicationsUploadedTemplate.builder()
            .callout("Lastname, SACCCCCCCC5676576567")
            .lastName("Smith")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2Tab")
            .documentLink(jsonFileObject.toMap())
            .applicationTypes(Arrays.asList("C2", "C13A - Special guardianship order"))
            .build();
    }
}
