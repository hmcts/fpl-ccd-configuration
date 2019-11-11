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
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;

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
    private IdamApi idamApi;

    @Mock
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Nested
    class C2UploadedNotificationChecks {
        final String subjectLine = "Lastname, SACCCCCCCC5676576567";
        final Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put("subjectLine", subjectLine)
            .put("hearingDetailsCallout", subjectLine)
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        @BeforeEach
        void before() throws IOException {
            given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
                .willReturn("Example Local Authority");

            given(c2UploadedEmailContentProvider.buildC2UploadNotification(callbackRequest().getCaseDetails()))
                .willReturn(parameters);
        }

        @Test
        void shouldNotNotifyHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
            given(idamApi.retrieveUserDetails(AUTH_TOKEN))
                .willReturn(new UserDetails("1", "hmcts-admin@test.com",
                    "Hmcts", "Test", HMCTS_ADMIN.getRoles()));

            notificationHandler.sendNotificationForC2Upload(new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationClient, never())
                .sendEmail(eq(C2_UPLOAD_NOTIFICATION_TEMPLATE), eq("hmcts-admin@test.com"),
                    eq(parameters), eq("12345"));
        }

        @Test
        void shouldNotifyNonHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
            given(idamApi.retrieveUserDetails(AUTH_TOKEN))
                .willReturn(new UserDetails("1", "hmcts-non-admin@test.com",
                    "Hmcts", "Test", LOCAL_AUTHORITY.getRoles()));

            given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
                .willReturn(new Court(COURT_NAME, "hmcts-non-admin@test.com"));

            notificationHandler.sendNotificationForC2Upload(new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

            verify(notificationClient, times(1)).sendEmail(
                eq(C2_UPLOAD_NOTIFICATION_TEMPLATE), eq("hmcts-non-admin@test.com"), eq(parameters), eq("12345"));
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

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq(COURT_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
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
            eq(CAFCASS_SUBMISSION_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
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

        notificationHandler.sendNotificationToGatekeeper(new NotifyGatekeeperEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
    }

    @Test
    void shouldNotifyCafcassOfIssuedStandardDirectionsOrder() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(cafcassEmailContentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.notifyCafcassOfIssuedStandardDirectionsOrder(new StandardDirectionsOrderIssuedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE), eq(CAFCASS_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedStandardDirectionsOrder() throws IOException, NotificationClientException {
        final Map<String, Object> expectedParameters = getStandardDirectionTemplateParameters();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration.LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(localAuthorityEmailContentProvider.buildLocalAuthorityStandardDirectionOrderIssuedNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        notificationHandler.notifyLocalAuthorityOfIssuedStandardDirectionsOrder(new StandardDirectionsOrderIssuedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(STANDARD_DIRECTION_ORDER_ISSUED_TEMPLATE), eq(LOCAL_AUTHORITY_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        final Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "6789")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate","21 October 2020")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        return expectedParameters;
    }
}
