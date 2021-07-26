package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.additionalapplicationsuploaded.AdditionalApplicationsUploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AdditionalApplicationsUploadedEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
class AdditionalApplicationsUploadedEventHandlerTest {
    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @MockBean
    private FeatureToggleService toggleService;

    @Autowired
    private AdditionalApplicationsUploadedEventHandler additionalApplicationsUploadedEventHandler;

    final String subjectLine = "Lastname, SACCCCCCCC5676576567";
    AdditionalApplicationsUploadedTemplate additionalApplicationsParameters =
        getAdditionalApplicationsUploadedTemplateParameters();

    @BeforeEach
    void before() {
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
    }

    @AfterEach
    void resetInvocations() {
        reset(notificationService);
        reset(inboxLookupService);
        reset(additionalApplicationsUploadedEmailContentProvider);
    }

    @Test
    void shouldNotifyNonHmctsAdminOnAdditionalApplicationsUpload() {
        CaseData caseData = caseData();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()))
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, "hmcts-non-admin@test.com",
                COURT_CODE));

        given(additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData))
            .willReturn(additionalApplicationsParameters);

        additionalApplicationsUploadedEventHandler.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE,
            "hmcts-non-admin@test.com",
            additionalApplicationsParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscAdminOnAdditionalApplicationsUploadWhenCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .sendToCtsc("Yes")
            .build();

        given(idamClient.getUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoleNames()).build());

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(additionalApplicationsUploadedEmailContentProvider
            .getNotifyData(caseData))
            .willReturn(additionalApplicationsParameters);

        additionalApplicationsUploadedEventHandler.notifyAdmin(new AdditionalApplicationsUploadedEvent(caseData));

        verify(notificationService).sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            additionalApplicationsParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyHmctsAdminOnAdditionalApplicationsUpload() {
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
            .callout(subjectLine)
            .respondentLastName("Smith")
            .childLastName("Jones")
            .caseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345#C2Tab")
            .documentLink(jsonFileObject.toMap())
            .applicationTypes(Arrays.asList("C2", "C13A - Special guardianship order"))
            .build();
    }
}
