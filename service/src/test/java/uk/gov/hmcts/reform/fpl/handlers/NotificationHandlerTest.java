package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@SuppressWarnings("LineLength")
@ExtendWith(SpringExtension.class)
class NotificationHandlerTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String CAFCASS_EMAIL_ADDRESS = "FamilyPublicLaw+cafcass@gmail.com";
    private static final String CAFCASS_NAME = "cafcass";
    private static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    @Mock
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private CafcassEmailContentProviderSDOIssued cafcassEmailContentProviderSDOIssued;

    @Mock
    private GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @Mock
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Mock
    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    @Mock
    private DateFormatterService dateFormatterService;

    @Mock
    private IdamApi idamApi;

    @Mock
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Mock
    private InboxLookupService inboxLookupService;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Nested
    class C2UploadedNotificationChecks {
        final String mostRecentUploadedDocumentUrl =
            "http://fake-document-gateway/documents/79ec80ec-7be6-493b-b4e6-f002f05b7079/binary";
        final String subjectLine = "Lastname, SACCCCCCCC5676576567";
        final Map<String, Object> c2Parameters = ImmutableMap.<String, Object>builder()
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        final Map<String, Object> orderCafcassParameters = ImmutableMap.<String, Object>builder()
            .putAll(c2Parameters)
            .put("localAuthorityOrCafcass", CAFCASS_NAME)
            .put("linkToDocument", mostRecentUploadedDocumentUrl)
            .build();

        final Map<String, Object> orderLocalAuthorityParameters = ImmutableMap.<String, Object>builder()
            .putAll(c2Parameters)
            .put("localAuthorityOrCafcass", LOCAL_AUTHORITY_NAME)
            .put("linkToDocument", mostRecentUploadedDocumentUrl)
            .build();

        @BeforeEach
        void before() throws IOException {
            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn("Example Local Authority");

            final LocalDate hearingDate = LocalDate.now().plusMonths(4);

            given(dateFormatterService.formatLocalDateToString(hearingDate, FormatStyle.MEDIUM))
                .willReturn(hearingDate.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK)));

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS));

            given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
                .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

            given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
                .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(callbackRequest().getCaseDetails()))
                .willReturn(c2Parameters);

            given(orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, mostRecentUploadedDocumentUrl))
                .willReturn(orderLocalAuthorityParameters);

            given(orderEmailContentProvider.buildOrderNotificationParametersForCafcass(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, mostRecentUploadedDocumentUrl))
                .willReturn(orderCafcassParameters);
        }

        @Test
        void shouldNotNotifyHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-admin@test.com").roles(HMCTS_ADMIN.getRoles()).build());

            notificationHandler.sendNotificationForC2Upload(
                new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationClient, never())
                .sendEmail(eq(C2_UPLOAD_NOTIFICATION_TEMPLATE), eq("hmcts-admin@test.com"),
                    eq(c2Parameters), eq("12345"));
        }

        @Test
        void shouldNotifyNonHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoles()).build());

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, "hmcts-non-admin@test.com"));

            notificationHandler.sendNotificationForC2Upload(
                new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationClient, times(1)).sendEmail(
                eq(C2_UPLOAD_NOTIFICATION_TEMPLATE), eq("hmcts-non-admin@test.com"), eq(c2Parameters), eq("12345"));
        }

        @Test
        void shouldNotifyPartiesOnOrderSubmission() throws IOException, NotificationClientException {
            notificationHandler.sendNotificationForOrder(
                new GeneratedOrderEvent(callbackRequest(), AUTH_TOKEN, USER_ID,
                    mostRecentUploadedDocumentUrl));

            verify(notificationClient, times(1)).sendEmail(
                eq(ORDER_NOTIFICATION_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                eq(orderLocalAuthorityParameters), eq("12345"));

            verify(notificationClient, times(1)).sendEmail(
                eq(ORDER_NOTIFICATION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
                eq(orderCafcassParameters), eq("12345"));
        }
    }

    @Test
    void shouldSendEmailToHmcts() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("court", COURT_NAME)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("orders0", "^Emergency protection order")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("directionsAndInterim", "^Information on the whereabouts of the child")
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(hmctsEmailContentProvider.buildHmctsSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.sendNotificationToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq(COURT_EMAIL_ADDRESS),
            eq(expectedParameters), eq("12345"));
    }

    @Test
    void shouldSendEmailToCafcass() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("cafcass", CAFCASS_NAME)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("orders0", "^Emergency protection order")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("directionsAndInterim", "^Information on the whereabouts of the child")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(cafcassEmailContentProvider.buildCafcassSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.sendNotificationToCafcass(new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS),
            eq(expectedParameters), eq("12345"));
    }

    @Test
    void shouldSendEmailToGatekeeper() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("orders0", "^Emergency protection order")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("directionsAndInterim", "^Information on the whereabouts of the child")
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(gatekeeperEmailContentProvider.buildGatekeeperNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.sendNotificationToGatekeeper(
            new NotifyGatekeeperEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL_ADDRESS),
            eq(expectedParameters), eq("12345"));
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrder() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(
            callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.notifyCafcassOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS), eq(expectedParameters),
            eq("12345"));
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrder() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(
            callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        given(
            inboxLookupService.getNotificationRecipientEmail(callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        notificationHandler.notifyLocalAuthorityOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS), eq(expectedParameters),
            eq("12345"));
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "6789")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate", "21 October 2020")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
