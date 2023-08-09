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
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {C2UploadedEventHandler.class})
class C2UploadedEventHandlerTest {
    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @MockBean
    private CourtService courtService;

    @Autowired
    private C2UploadedEventHandler c2UploadedEventHandler;

    private final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().build();

    private final C2UploadedTemplate c2Parameters = getC2UploadedTemplateParameters();

    @BeforeEach
    void before() {
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
    }

    @Test
    void shouldNotifyHmctsAdminWhenC2UploadedByNonHmctsAdmin() {
        CaseData caseData = caseData().toBuilder().build();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(courtService.getCourtEmail(caseData))
            .willReturn("hmcts-admin@test.com");

        given(c2UploadedEmailContentProvider.getNotifyData(caseData,
            c2DocumentBundle.getDocument()))
            .willReturn(c2Parameters);

        c2UploadedEventHandler.notifyAdmin(new C2UploadedEvent(caseData, c2DocumentBundle));

        verify(notificationService).sendEmail(
            C2_UPLOAD_NOTIFICATION_TEMPLATE,
            "hmcts-admin@test.com",
            c2Parameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyHmctsAdminWhenC2UploadedByHmctsAdmin() {
        CaseData caseData = caseData();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().roles(HMCTS_ADMIN.getRoleNames()).build());

        c2UploadedEventHandler.notifyAdmin(new C2UploadedEvent(caseData, c2DocumentBundle));

        verifyNoInteractions(notificationService);
    }

    private C2UploadedTemplate getC2UploadedTemplateParameters() {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return C2UploadedTemplate.builder()
            .callout("Lastname, SACCCCCCCC5676576567")
            .respondentLastName("Smith")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2Tab")
            .documentLink(jsonFileObject.toMap())
            .build();
    }
}
