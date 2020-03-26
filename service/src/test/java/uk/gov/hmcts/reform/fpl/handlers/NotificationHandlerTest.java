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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GeneratedOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GeneratedOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderIssuedEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
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
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    private static final String COURT_CODE = "11";
    private static final String CTSC_INBOX = "Ctsc+test@gmail.com";
    private final byte[] documentContents = {1, 2, 3};

    @Mock
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @Mock
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Mock
    private GeneratedOrderEmailContentProvider orderEmailContentProvider;

    @Mock
    private OrderIssuedEmailContentProvider orderIssuedEmailContentProvider;

    @Mock
    private IdamApi idamApi;

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private RepresentativeService representativeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    @BeforeEach
    void setup() {
        notificationHandler = new NotificationHandler(hmctsCourtLookupConfiguration,
            orderEmailContentProvider, orderIssuedEmailContentProvider, inboxLookupService, representativeService,
            objectMapper, ctscEmailLookupConfiguration, notificationService);
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
        void shouldNotifyPartiesOnOrderSubmission() throws IOException {
            notificationHandler.sendEmailsForOrder(new GeneratedOrderEvent(callbackRequest(),
                AUTH_TOKEN, USER_ID, mostRecentUploadedDocumentUrl, documentContents));

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
                AUTH_TOKEN, USER_ID, mostRecentUploadedDocumentUrl, documentContents));

            verify(notificationService).sendEmail(
                ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN,
                CTSC_INBOX,
                getExpectedParametersForAdminWhenNoRepresentativesServedByPost(true),
                "12345");
        }
    }

    @Nested
    class NoticeOfPlacementOrderNotification {

        @BeforeEach
        void setup() throws IOException {
            given(inboxLookupService.getNotificationRecipientEmail(
                callbackRequest().getCaseDetails(), LOCAL_AUTHORITY_CODE)).willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);
        }
    }

    private List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
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
