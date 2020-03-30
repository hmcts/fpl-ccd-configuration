package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.USER_ID;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {C2UploadedEventHandler.class, JacksonAutoConfiguration.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
public class C2UploadedEventHandlerTest {
    @MockBean
    private IdamApi idamApi;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @MockBean
    private RequestData requestData;

    @Autowired
    private C2UploadedEventHandler c2UploadedEventHandler;

    @Nested
    class C2UploadedNotificationChecks {
        final String subjectLine = "Lastname, SACCCCCCCC5676576567";
        final Map<String, Object> c2Parameters = ImmutableMap.<String, Object>builder()
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        @BeforeEach
        void before() throws IOException {
            CaseDetails caseDetails = callbackRequest().getCaseDetails();

            given(requestData.authorisation()).willReturn(AUTH_TOKEN);

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
                .willReturn(c2Parameters);
        }

        @Test
        void shouldNotifyNonHmctsAdminOnC2Upload() throws IOException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoles()).build());

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, "hmcts-non-admin@test.com",
                    COURT_CODE));

            c2UploadedEventHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-non-admin@test.com", c2Parameters, "12345");
        }

        @Test
        void shouldNotifyCtscAdminOnC2UploadWhenCtscIsEnabled() throws IOException {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
                .willReturn(c2Parameters);

            c2UploadedEventHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest, AUTH_TOKEN, USER_ID));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE,
                CTSC_INBOX,
                c2Parameters,
                "12345");
        }

        @Test
        void shouldNotNotifyHmctsAdminOnC2Upload() throws IOException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-admin@test.com").roles(HMCTS_ADMIN.getRoles()).build());

            c2UploadedEventHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationService, never())
                .sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-admin@test.com",
                    c2Parameters, "12345");
        }
    }
}
