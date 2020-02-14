package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAdminOrderIssuedTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
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
    private static final String COURT_CODE = "11";
    private final byte[] documentContents = {1, 2, 3};

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
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Mock
    private DateFormatterService dateFormatterService;

    @Mock
    private IdamApi idamApi;

    @Mock
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Mock
    private PlacementApplicationContentProvider placementApplicationContentProvider;

    @Mock
    private RepresentativeService representativeService;

    @Autowired
    private ObjectMapper objectMapper;

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

        final Map<String, Object> orderLocalAuthorityParameters = ImmutableMap.<String, Object>builder()
            .putAll(c2Parameters)
            .put("localAuthorityOrCafcass", LOCAL_AUTHORITY_NAME)
            .put("linkToDocument", mostRecentUploadedDocumentUrl)
            .build();

        @BeforeEach
        void before() throws IOException {
            CaseDetails caseDetails = callbackRequest().getCaseDetails();

            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn("Example Local Authority");

            final LocalDate hearingDate = LocalDate.now().plusMonths(4);

            given(dateFormatterService.formatLocalDateToString(hearingDate, FormatStyle.MEDIUM))
                .willReturn(hearingDate.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK)));

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
                .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
                .willReturn(c2Parameters);

            given(orderEmailContentProvider.buildOrderNotificationParametersForLocalAuthority(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, mostRecentUploadedDocumentUrl))
                .willReturn(orderLocalAuthorityParameters);

            given(orderIssuedEmailContentProvider.buildOrderNotificationParametersForHmctsAdmin(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost());
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
                .willReturn(new Court(COURT_NAME, "hmcts-non-admin@test.com", COURT_CODE));

            notificationHandler.sendNotificationForC2Upload(
                new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationClient, times(1)).sendEmail(
                eq(C2_UPLOAD_NOTIFICATION_TEMPLATE), eq("hmcts-non-admin@test.com"), eq(c2Parameters), eq("12345"));
        }

        @Test
        void shouldNotifyPartiesOnOrderSubmission() throws IOException, NotificationClientException {
            notificationHandler.sendNotificationsForGeneratedOrder(new GeneratedOrderEvent(callbackRequest(),
                AUTH_TOKEN, USER_ID, mostRecentUploadedDocumentUrl, documentContents));

            verify(notificationClient).sendEmail(
                eq(ORDER_NOTIFICATION_TEMPLATE_FOR_LA),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                eq(orderLocalAuthorityParameters),
                eq("12345"));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(COURT_EMAIL_ADDRESS),
                eq(getExpectedParametersForAdminWhenNoRepresentativesServedByPost()),
                eq("12345"));
        }
    }

    @Nested
    class CaseManagementOrderNotificationTests {
        private final Map<String, Object> expectedCMOIssuedNotificationParameters =
            getCMOIssuedCaseLinkNotificationParameters();
        private final Map<String, Object> expectedCMOIssuedNotificationParametersForRepresentative =
            getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative();
        private final Map<String, Object> expectedCMOReadyForJudgeNotificationParameters =
            getCMOReadyForJudgeNotificationParameters();
        private final Map<String, Object> expectedCMORejectedNotificationParameters =
            getCMORejectedCaseLinkNotificationParameters();

        private NotificationHandler cmoNotificationHandler;

        @BeforeEach
        void setup() {
            given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
                .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_NAME);

            // did this to enable ObjectMapper injection
            // TODO: 17/12/2019 nice to refactor to make cleaner
            cmoNotificationHandler = new NotificationHandler(hmctsCourtLookupConfiguration, cafcassLookupConfiguration,
                hmctsEmailContentProvider, cafcassEmailContentProvider, cafcassEmailContentProviderSDOIssued,
                gatekeeperEmailContentProvider, c2UploadedEmailContentProvider, orderEmailContentProvider,
                orderIssuedEmailContentProvider, localAuthorityEmailContentProvider, notificationClient, idamApi,
                inboxLookupService, caseManagementOrderEmailContentProvider, placementApplicationContentProvider,
                representativeService, localAuthorityNameLookupConfiguration, objectMapper);
        }

        @Test
        void shouldNotifyAdminAndLocalAuthorityOfCMOIssued() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
                LOCAL_AUTHORITY_NAME))
                .willReturn(expectedCMOIssuedNotificationParameters);

            given(orderIssuedEmailContentProvider.buildOrderNotificationParametersForHmctsAdmin(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, CMO))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost());

            cmoNotificationHandler.sendNotificationsForIssuedCaseManagementOrder(
                new CaseManagementOrderIssuedEvent(callbackRequest, AUTH_TOKEN, USER_ID, documentContents));

            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                eq(expectedCMOIssuedNotificationParameters),
                eq("12345"));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(COURT_EMAIL_ADDRESS),
                eq(getExpectedParametersForAdminWhenNoRepresentativesServedByPost()),
                eq("12345"));
        }

        @Test
        void shouldNotifyRepresentativesOfCMOIssued() throws Exception {
            CallbackRequest callbackRequest = buildCallbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            CaseData caseData = buildCaseDataWithRepresentatives();

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
                DIGITAL_SERVICE))
                .willReturn(expectedRepresentatives());

            given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
                "Jon Snow"))
                .willReturn(expectedCMOIssuedNotificationParametersForRepresentative);

            cmoNotificationHandler.sendNotificationsForIssuedCaseManagementOrder(
                new CaseManagementOrderIssuedEvent(callbackRequest, AUTH_TOKEN, USER_ID, documentContents));

            verify(notificationClient).sendEmail(
                eq(CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE),
                eq("abc@example.com"),
                eq(expectedCMOIssuedNotificationParametersForRepresentative),
                eq("12345"));
        }

        @Test
        void shouldNotifyLocalAuthorityOfCMORejected() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(caseDetails))
                .willReturn(expectedCMORejectedNotificationParameters);

            cmoNotificationHandler.notifyLocalAuthorityOfRejectedCaseManagementOrder(
                new CaseManagementOrderRejectedEvent(callbackRequest, AUTH_TOKEN, USER_ID));

            verify(notificationClient).sendEmail(
                eq(CMO_REJECTED_BY_JUDGE_TEMPLATE),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                eq(expectedCMORejectedNotificationParameters),
                eq("12345"));
        }

        @Test
        void shouldNotifyAdminOfCMOReadyForJudgeReview() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest().getCaseDetails();

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(
                caseManagementOrderEmailContentProvider.buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
                .willReturn(expectedCMOReadyForJudgeNotificationParameters);

            cmoNotificationHandler.sendNotificationForCaseManagementOrderReadyForJudgeReview(
                new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest, AUTH_TOKEN, USER_ID));

            verify(notificationClient).sendEmail(
                eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE), eq(COURT_EMAIL_ADDRESS),
                eq(expectedCMOReadyForJudgeNotificationParameters), eq("12345"));
        }

        private ImmutableMap<String, Object> getCMOIssuedCaseLinkNotificationParameters() {
            return ImmutableMap.<String, Object>builder()
                .put("localAuthorityNameOrRepresentativeFullName", LOCAL_AUTHORITY_NAME)
                .putAll(expectedCommonCMONotificationParameters())
                .build();
        }

        private ImmutableMap<String, Object> getCMOReadyForJudgeNotificationParameters() {
            return ImmutableMap.<String, Object>builder()
                .putAll(expectedCommonCMONotificationParameters())
                .build();
        }

        private CaseDetails buildCaseDetailsWithRepresentatives() throws IOException {
            CaseDetails caseDetails = callbackRequest().getCaseDetails();
            Map<String, Object> caseData = caseDetails.getData();

            caseData.put("representatives", createRepresentatives(DIGITAL_SERVICE));
            return caseDetails.toBuilder()
                .data(caseData)
                .build();
        }

        private CaseData buildCaseDataWithRepresentatives() {
            return CaseData.builder()
                .representatives(createRepresentatives(DIGITAL_SERVICE))
                .build();
        }

        private Map<String, Object> getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative() {
            return ImmutableMap.<String, Object>builder()
                .put("localAuthorityNameOrRepresentativeFullName", "Jon Snow")
                .putAll(expectedCommonCMONotificationParameters())
                .build();
        }

        private CallbackRequest buildCallbackRequest() throws IOException {
            return CallbackRequest.builder()
                .caseDetails(buildCaseDetailsWithRepresentatives())
                .build();
        }

        private List<Representative> expectedRepresentatives() {
            return ImmutableList.of(Representative.builder()
                .email("abc@example.com")
                .fullName("Jon Snow")
                .servingPreferences(DIGITAL_SERVICE)
                .build());
        }

        private Map<String, Object> expectedCommonCMONotificationParameters() {
            String subjectLine = "Lastname, SACCCCCCCC5676576567";
            return ImmutableMap.of("subjectLineWithHearingDate", subjectLine,
                "reference", "12345",
                "caseUrl", String.format("null/case/%s/%s/12345", JURISDICTION, CASE_TYPE));
        }

        private Map<String, Object> getCMORejectedCaseLinkNotificationParameters() {
            return ImmutableMap.<String, Object>builder()
                .put("requestedChanges", "Please make these changes XYZ")
                .putAll(expectedCommonCMONotificationParameters())
                .build();
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
            .put("timeFrameValue", "same day")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

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
            .put("timeFrameValue", "same day")
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

    @Nested
    class NoticeOfPlacementOrderNotification {
        private NotificationHandler placementNotificationHandler;

        @BeforeEach
        void setup() throws IOException {
            given(inboxLookupService.getNotificationRecipientEmail(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            placementNotificationHandler = new NotificationHandler(hmctsCourtLookupConfiguration,
                cafcassLookupConfiguration, hmctsEmailContentProvider, cafcassEmailContentProvider,
                cafcassEmailContentProviderSDOIssued, gatekeeperEmailContentProvider, c2UploadedEmailContentProvider,
                orderEmailContentProvider, orderIssuedEmailContentProvider, localAuthorityEmailContentProvider,
                notificationClient, idamApi, inboxLookupService, caseManagementOrderEmailContentProvider,
                placementApplicationContentProvider, representativeService, localAuthorityNameLookupConfiguration,
                objectMapper);
        }

        @Test
        void shouldSendNotificationForPlacementOrderUploaded() throws IOException, NotificationClientException {
            Map<String, Object> parameters = Map.of("respondentLastName", "Nelson",
                "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 1L));

            given(localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(
                callbackRequest().getCaseDetails())).willReturn(parameters);

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(orderIssuedEmailContentProvider.buildOrderNotificationParametersForHmctsAdmin(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, NOTICE_OF_PLACEMENT_ORDER))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost());

            placementNotificationHandler.sendNotificationForNoticeOfPlacementOrderUploaded(
                new NoticeOfPlacementOrderUploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID, documentContents));

            verify(notificationClient, times(1)).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq(LOCAL_AUTHORITY_EMAIL_ADDRESS),
                eq(parameters),
                eq("12345"));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(COURT_EMAIL_ADDRESS),
                eq(getExpectedParametersForAdminWhenNoRepresentativesServedByPost()),
                eq("12345"));
        }
    }

    @Test
    void shouldNotifyAdminOfPlacementApplicationUpload() throws Exception {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        final Map<String, Object> expectedParameters = getExpectedPlacementNotificationParameters();

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

        given(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseDetails))
            .willReturn(expectedParameters);

        notificationHandler.notifyAdminOfPlacementApplicationUpload(
            new PlacementApplicationEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationClient).sendEmail(
            eq(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), eq(COURT_EMAIL_ADDRESS),
            eq(expectedParameters), eq("12345"));
    }

    private Map<String, Object> getExpectedPlacementNotificationParameters() {
        return ImmutableMap.of(
            "respondentLastName", "Moley",
            "caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345"
        );
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
