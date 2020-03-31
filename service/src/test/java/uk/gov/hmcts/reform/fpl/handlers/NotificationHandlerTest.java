package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.events.NoticeOfPlacementOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForAdminWhenNoRepresentativesServedByPost;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;

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
    private static final String PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS = "barney@rubble.com";
    private static final String PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL = "fred@flinstone.com";
    private static final String CTSC_INBOX = "Ctsc+test@gmail.com";
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
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

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

    @Mock
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @Mock
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    @Mock
    private RequestData requestData;

    @BeforeEach
    void setup() throws IOException {
        notificationHandler = new NotificationHandler(hmctsCourtLookupConfiguration,
            cafcassLookupConfiguration, hmctsEmailContentProvider, partyAddedToCaseContentProvider,
            cafcassEmailContentProvider, cafcassEmailContentProviderSDOIssued, gatekeeperEmailContentProvider,
            c2UploadedEmailContentProvider, orderEmailContentProvider, orderIssuedEmailContentProvider,
            localAuthorityEmailContentProvider, failedPBAPaymentContentProvider, idamApi, inboxLookupService,
            caseManagementOrderEmailContentProvider, placementApplicationContentProvider, representativeService,
            localAuthorityNameLookupConfiguration, objectMapper, ctscEmailLookupConfiguration, notificationService);

        given(inboxLookupService.getNotificationRecipientEmail(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
    }

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
            CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn("Example Local Authority");

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

            given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

            given(orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER))
                .willReturn(getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));

            given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
                .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());
        }

        @Test
        void shouldNotNotifyHmctsAdminOnC2Upload() throws IOException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-admin@test.com").roles(HMCTS_ADMIN.getRoles()).build());

            notificationHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest(), requestData));

            verify(notificationService, never())
                .sendEmail(C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-admin@test.com",
                    c2Parameters, "12345");
        }

        @Test
        void shouldNotifyNonHmctsAdminOnC2Upload() throws IOException {
            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub("hmcts-non-admin@test.com").roles(LOCAL_AUTHORITY.getRoles()).build());

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, "hmcts-non-admin@test.com", COURT_CODE));

            notificationHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest(), requestData));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE, "hmcts-non-admin@test.com", c2Parameters, "12345");
        }

        @Test
        void shouldNotifyCtscAdminOnC2UploadWhenCtscIsEnabled() throws IOException {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

            given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(caseDetails))
                .willReturn(c2Parameters);

            notificationHandler.sendEmailForC2Upload(
                new C2UploadedEvent(callbackRequest, requestData));

            verify(notificationService).sendEmail(
                C2_UPLOAD_NOTIFICATION_TEMPLATE,
                CTSC_INBOX,
                c2Parameters,
                "12345");
        }

        @Test
        void shouldNotifyPartiesOnOrderSubmission() throws IOException {
            notificationHandler.sendEmailsForOrder(new GeneratedOrderEvent(callbackRequest(),
                requestData, mostRecentUploadedDocumentUrl, documentContents));

            verify(notificationService).sendEmail(
                ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA,
                LOCAL_AUTHORITY_EMAIL_ADDRESS,
                orderLocalAuthorityParameters,
                "12345");

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                COURT_EMAIL_ADDRESS,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
                "12345");

            verify(notificationService).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
                eq("barney@rubble.com"),
                dataCaptor.capture(),
                eq("12345"));

            assertEquals(dataCaptor.getValue(), getExpectedParametersForRepresentatives(BLANK_ORDER.getLabel(), true));
        }

        @Test
        void shouldNotifyCtsAdminOnOrderSubmission() throws IOException {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();

            given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
                callbackRequest.getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, GENERATED_ORDER))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

            given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
                UserInfo.builder().sub(CTSC_INBOX).roles(LOCAL_AUTHORITY.getRoles()).build());

            given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

            notificationHandler.sendEmailsForOrder(new GeneratedOrderEvent(callbackRequest,
                requestData, mostRecentUploadedDocumentUrl, documentContents));

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                CTSC_INBOX,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
                "12345");
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

        @BeforeEach
        void setup() {
            given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
                .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_NAME);
        }

        @Test
        void shouldNotifyHmctsAdminAndLocalAuthorityOfCMOIssued() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
                LOCAL_AUTHORITY_NAME))
                .willReturn(expectedCMOIssuedNotificationParameters);

            given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, CMO))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

            notificationHandler.sendEmailsForIssuedCaseManagementOrder(
                new CaseManagementOrderIssuedEvent(callbackRequest, requestData, documentContents));

            verify(notificationService).sendEmail(
                CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
                LOCAL_AUTHORITY_EMAIL_ADDRESS,
                expectedCMOIssuedNotificationParameters,
                "12345");

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                COURT_EMAIL_ADDRESS,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
                "12345");
        }

        @Test
        void shouldNotifyCtscAdminOfCMOIssued() throws Exception {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

            given(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(caseDetails,
                LOCAL_AUTHORITY_NAME))
                .willReturn(expectedCMOIssuedNotificationParameters);

            given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
                callbackRequest.getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, CMO))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true));

            notificationHandler.sendEmailsForIssuedCaseManagementOrder(
                new CaseManagementOrderIssuedEvent(callbackRequest, requestData, documentContents));

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                CTSC_INBOX,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
                "12345");
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

            notificationHandler.sendEmailsForIssuedCaseManagementOrder(
                new CaseManagementOrderIssuedEvent(callbackRequest, requestData, documentContents));

            verify(notificationService).sendEmail(
                CMO_ORDER_ISSUED_CASE_LINK_NOTIFICATION_TEMPLATE,
                "abc@example.com",
                expectedCMOIssuedNotificationParametersForRepresentative,
                "12345");
        }

        @Test
        void shouldNotifyLocalAuthorityOfCMORejected() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
                .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

            given(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(caseDetails))
                .willReturn(expectedCMORejectedNotificationParameters);

            notificationHandler.notifyLocalAuthorityOfRejectedCaseManagementOrder(
                new CaseManagementOrderRejectedEvent(callbackRequest, requestData));

            verify(notificationService).sendEmail(
                CMO_REJECTED_BY_JUDGE_TEMPLATE,
                LOCAL_AUTHORITY_EMAIL_ADDRESS,
                expectedCMORejectedNotificationParameters,
                "12345");
        }

        @Test
        void shouldNotifyHmctsAdminOfCMOReadyForJudgeReviewWhenCtscIsDisabled() throws Exception {
            CallbackRequest callbackRequest = callbackRequest();
            CaseDetails caseDetails = callbackRequest().getCaseDetails();

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(caseManagementOrderEmailContentProvider
                .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
                .willReturn(expectedCMOReadyForJudgeNotificationParameters);

            notificationHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
                new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest, requestData));

            verify(notificationService).sendEmail(
                CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
                COURT_EMAIL_ADDRESS,
                expectedCMOReadyForJudgeNotificationParameters,
                "12345");
        }

        @Test
        void shouldNotifyCtscAdminOfCMOReadyForJudgeReviewWhenCtscIsEnabled() throws Exception {
            CallbackRequest callbackRequest = appendSendToCtscOnCallback();
            CaseDetails caseDetails = callbackRequest.getCaseDetails();

            given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

            given(caseManagementOrderEmailContentProvider
                .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
                .willReturn(expectedCMOReadyForJudgeNotificationParameters);

            notificationHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
                new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest, requestData));

            verify(notificationService).sendEmail(
                CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
                CTSC_INBOX,
                expectedCMOReadyForJudgeNotificationParameters,
                "12345");
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
    void shouldSendEmailToHmctsAdminWhenCtscIsDisabled() throws IOException {
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

        notificationHandler.sendEmailToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldSendEmailToCtscAdminWhenCtscIsEnabled() throws IOException {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

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

        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

        given(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(expectedParameters);

        notificationHandler.sendEmailToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            CTSC_INBOX,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldSendEmailToCafcass() throws IOException {
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

        notificationHandler.sendEmailToCafcass(new SubmittedCaseEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE, CAFCASS_EMAIL_ADDRESS,
            expectedParameters, "12345");
    }

    @Test
    void shouldSendEmailToGatekeeper() throws IOException {
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

        notificationHandler.sendEmailToGatekeeper(
            new NotifyGatekeeperEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL_ADDRESS,
            expectedParameters, "12345");
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrder() throws IOException {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(
            callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.notifyCafcassOfIssuedStandardDirectionsOrder(
            new StandardDirectionsOrderIssuedEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE,
            CAFCASS_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrder() throws IOException {
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
            new StandardDirectionsOrderIssuedEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE, LOCAL_AUTHORITY_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    @Nested
    class NoticeOfPlacementOrderNotification {

        @BeforeEach
        void setup() throws IOException {
            given(inboxLookupService.getNotificationRecipientEmail(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);
        }

        @Test
        void shouldSendEmailForPlacementOrderUploaded() throws IOException {
            Map<String, Object> parameters = Map.of("respondentLastName", "Nelson",
                "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 1L));
            CaseDetails caseDetails = callbackRequest().getCaseDetails();
            CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

            given(localAuthorityEmailContentProvider.buildNoticeOfPlacementOrderUploadedNotification(
                caseDetails)).willReturn(parameters);

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

            given(orderIssuedEmailContentProvider.buildNotificationParametersForHmctsAdmin(
                caseDetails, LOCAL_AUTHORITY_CODE, documentContents, NOTICE_OF_PLACEMENT_ORDER))
                .willReturn(getExpectedParametersForAdminWhenNoRepresentativesServedByPost(false));

            given(orderIssuedEmailContentProvider.buildNotificationParametersForRepresentatives(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE, documentContents, NOTICE_OF_PLACEMENT_ORDER))
                .willReturn(getExpectedParametersForRepresentatives(NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));

            given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
                .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

            notificationHandler.sendEmailForNoticeOfPlacementOrderUploaded(
                new NoticeOfPlacementOrderUploadedEvent(callbackRequest(), requestData, documentContents));

            verify(notificationService).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                LOCAL_AUTHORITY_EMAIL_ADDRESS,
                parameters,
                "12345");

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                COURT_EMAIL_ADDRESS,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(false),
                "12345");

            verify(notificationService).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
                eq("barney@rubble.com"),
                dataCaptor.capture(),
                eq("12345"));

            assertEquals(dataCaptor.getValue(),
                getExpectedParametersForRepresentatives(NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));
        }
    }

    @Test
    void shouldNotifyHmctsAdminOfPlacementApplicationUploadWhenCtscIsDiabled() throws Exception {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        final Map<String, Object> expectedParameters = getExpectedPlacementNotificationParameters();

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

        given(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseDetails))
            .willReturn(expectedParameters);

        notificationHandler.notifyAdminOfPlacementApplicationUpload(
            new PlacementApplicationEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldSendEmailToPartiesWhenAddedToCase() throws IOException {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest().getCaseDetailsBefore();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData caseDataBefore = objectMapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

        final Map<String, Object> expectedEmailParameters = getPartyAddedByEmailNotificationParameters();
        final Map<String, Object> expectedDigitalParameters = getPartyAddedByDigitalServiceNotificationParameters();

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalRepresentativesForAddingPartiesToCase());

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalRepresentativesForAddingPartiesToCase());

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), EMAIL)).willReturn(expectedEmailParameters);

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), DIGITAL_SERVICE)).willReturn(expectedDigitalParameters);

        notificationHandler.sendEmailToPartiesAddedToCase(
            new PartyAddedToCaseEvent(callbackRequest(), requestData));

        verify(notificationService).sendEmail(
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE,
            PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS,
            expectedEmailParameters,
            "12345");

        verify(notificationService).sendEmail(
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE,
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL,
            expectedDigitalParameters,
            "12345");
    }

    @Test
    void shouldNotifyCtscAdminOfPlacementApplicationUploadWhenCtscIsEnabled() throws Exception {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        final Map<String, Object> expectedParameters = getExpectedPlacementNotificationParameters();

        given(ctscEmailLookupConfiguration.getEmail()).willReturn(CTSC_INBOX);

        given(placementApplicationContentProvider.buildPlacementApplicationNotificationParameters(caseDetails))
            .willReturn(expectedParameters);

        notificationHandler.notifyAdminOfPlacementApplicationUpload(
            new PlacementApplicationEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyLAWhenApplicationPBAPaymentFails() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        final Map<String, Object> expectedParameters = Map.of("applicationType", "C110a");

        given(failedPBAPaymentContentProvider.buildLANotificationParameters(C110A_APPLICATION))
            .willReturn(expectedParameters);

        notificationHandler.sendFailedPBAPaymentEmailToLocalAuthority(
            new FailedPBAPaymentEvent(callbackRequest, requestData, C110A_APPLICATION));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            expectedParameters,
            "12345");
    }

    @Test
    void shouldNotifyCtscWhenApplicationPBAPaymentFails() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        final Map<String, Object> expectedParameters = getCtscNotificationParametersForFailedPayment();

        given(failedPBAPaymentContentProvider.buildCtscNotificationParameters(callbackRequest
                .getCaseDetails(), C2_APPLICATION)).willReturn(expectedParameters);

        notificationHandler.sendFailedPBAPaymentEmailToCTSC(
            new FailedPBAPaymentEvent(callbackRequest, requestData, C2_APPLICATION));

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            CTSC_INBOX,
            expectedParameters,
            "12345");
    }

    private Map<String, Object> getCtscNotificationParametersForFailedPayment() {
        return Map.of("applicationType", "C2",
            "caseUrl", "caseUrl");
    }

    private List<Representative> getExpectedDigitalRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("fred@flinstone.com")
                .fullName("Fred Flinstone")
                .servingPreferences(DIGITAL_SERVICE)
                .build());
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
    }

    private Map<String, Object> getExpectedPlacementNotificationParameters() {
        return ImmutableMap.of(
            "respondentLastName", "Moley",
            "caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345"
        );
    }

    private Map<String, Object> getPartyAddedByEmailNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Moley")
            .put("familyManCaseNumber", "123")
            .put("reference", "12345")
            .build();
    }

    private Map<String, Object> getPartyAddedByDigitalServiceNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "Moley")
            .put("familyManCaseNumber", "123")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
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

    private CallbackRequest appendSendToCtscOnCallback() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> updatedCaseData = ImmutableMap.<String, Object>builder()
            .putAll(caseDetails.getData())
            .put("sendToCtsc", "Yes")
            .build();

        caseDetails.setData(updatedCaseData);
        callbackRequest.setCaseDetails(caseDetails);

        return callbackRequest;
    }
}
