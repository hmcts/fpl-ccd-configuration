package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForC2;
import uk.gov.hmcts.reform.fpl.model.notify.c2uploaded.C2UploadedTemplate;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {C2UploadedEventHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
public class C2UploadedEventHandlerTest {
    @MockBean
    private IdamApi idamApi;

    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private C2UploadedEventHandler c2UploadedEventHandler;

    @Nested
    class C2UploadedNotificationChecks {
        final String subjectLine = "Lastname, SACCCCCCCC5676576567";
        C2UploadedTemplate c2Parameters = getC2UploadedTemplateParameters();

        @BeforeEach
        void before() {
            CaseDetails caseDetails = callbackRequest().getCaseDetails();

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(callbackRequest().getCaseDetails()))
                .willReturn(c2Parameters);

            given(requestData.authorisation()).willReturn(AUTH_TOKEN);

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);
        }

        @Test
        void shouldNotifyNonHmctsAdminOnC2Upload() {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoles()).build());

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, "hmcts-non-admin@test.com",
                    COURT_CODE));

            c2UploadedEventHandler.sendNotifications(
                new C2UploadedEvent(callbackRequest()));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-non-admin@test.com", c2Parameters, "12345");
        }

        @Test
        void shouldNotifyCtscAdminOnC2UploadWhenCtscIsEnabled() {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
                .willReturn(c2Parameters);

            c2UploadedEventHandler.sendNotifications(
                new C2UploadedEvent(callbackRequest));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE,
                CTSC_INBOX,
                c2Parameters,
                "12345");
        }

        @Test
        void shouldNotNotifyHmctsAdminOnC2Upload() {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-admin@test.com").roles(HMCTS_ADMIN.getRoles()).build());

            c2UploadedEventHandler.sendNotifications(
                new C2UploadedEvent(callbackRequest()));

            verify(notificationService, never())
                .sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-admin@test.com",
                    c2Parameters, "12345");
        }

        @Test
        void shouldNotifyAllocatedJudgeOnC2UploadWhenAllocatedJudgeExistsAndEnabled() {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            AllocatedJudgeTemplateForC2 allocatedJudgeParametersForC2 = getAllocatedJudgeParametersForC2();

            given(featureToggleService.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).willReturn(true);

            given(c2UploadedEmailContentProvider.buildC2UploadNotificationForAllocatedJudge(caseDetails))
                .willReturn(allocatedJudgeParametersForC2);

            c2UploadedEventHandler.sendC2UploadedNotificationToAllocatedJudge(
                new C2UploadedEvent(callbackRequest));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE,
                ALLOCATED_JUDGE_EMAIL_ADDRESS,
                allocatedJudgeParametersForC2,
                "12345");
        }

        @Test
        void shouldNotifyAllocatedJudgeOnC2UploadWhenAllocatedJudgeExistsAndDisabled() {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            AllocatedJudgeTemplateForC2 allocatedJudgeParametersForC2 = getAllocatedJudgeParametersForC2();

            given(featureToggleService.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).willReturn(false);

            given(c2UploadedEmailContentProvider.buildC2UploadNotificationForAllocatedJudge(caseDetails))
                .willReturn(allocatedJudgeParametersForC2);

            c2UploadedEventHandler.sendC2UploadedNotificationToAllocatedJudge(
                new C2UploadedEvent(callbackRequest));

            verify(notificationService, never()).sendEmail(
                eq(C2_UPLOAD_NOTIFICATION_TEMPLATE_JUDGE),
                anyString(),
                anyMap(),
                anyString());
        }

        @Test
        void shouldNotNotifyAllocatedJudgeOnC2UploadWhenAllocatedJudgeDoesNotExist() {
            CaseDetails caseDetails = CaseDetails.builder().id(1L)
                .data(Map.of("caseLocalAuthority", "SA"))
                .build();

            CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(
                caseDetails).build();

            given(c2UploadedEmailContentProvider.buildC2UploadNotificationForAllocatedJudge(caseDetails))
                .willReturn(getAllocatedJudgeParametersForC2());

            c2UploadedEventHandler.sendC2UploadedNotificationToAllocatedJudge(
                new C2UploadedEvent(callbackRequest));

            verify(notificationService, never()).sendEmail(any(), any(), anyMap(), any());
        }

        private AllocatedJudgeTemplateForC2 getAllocatedJudgeParametersForC2() {
            AllocatedJudgeTemplateForC2 allocatedJudgeTemplateForC2 = new AllocatedJudgeTemplateForC2();

            allocatedJudgeTemplateForC2.setCaseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
            allocatedJudgeTemplateForC2.setCallout(subjectLine);
            allocatedJudgeTemplateForC2.setJudgeTitle("Her Honour Judge");
            allocatedJudgeTemplateForC2.setJudgeName("Byrne");
            allocatedJudgeTemplateForC2.setRespondentLastName("Smith");

            return allocatedJudgeTemplateForC2;
        }

        private C2UploadedTemplate getC2UploadedTemplateParameters() {
            String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENT), ISO_8859_1);
            JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

            C2UploadedTemplate uploadC2Template = new C2UploadedTemplate();

            uploadC2Template.setCallout(subjectLine);
            uploadC2Template.setRespondentLastName("Smith");
            uploadC2Template.setCaseUrl("null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
            uploadC2Template.setDocumentLink(jsonFileObject.toMap());

            return uploadC2Template;
        }
    }
}
