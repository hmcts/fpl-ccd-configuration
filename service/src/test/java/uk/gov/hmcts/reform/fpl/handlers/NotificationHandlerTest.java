package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C21OrderEvent;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.EmailNotificationService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.email.content.C21OrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.C21_ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.HMCTS_COURT_SUBMISSION_TEMPLATE;
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

    @Mock
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @Mock
    private C2UploadedEmailContentProvider c2UploadedEmailContentProvider;

    @Mock
    private C21OrderEmailContentProvider c21OrderEmailContentProvider;

    @Mock
    private IdamApi idamApi;

    @Mock
    private DateFormatterService dateFormatterService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Test
    void shouldNotNotifyHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
        final String SUBJ_LINE = "Lastname, SACCCCCCCC5676576567";
        final Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put("subjectLine", SUBJ_LINE)
            .put("hearingDetailsCallout", SUBJ_LINE)
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(idamApi.retrieveUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails("1", "hmcts-admin@test.com",
                "Hmcts", "Test", UserRole.HMCTS_ADMIN.getRoles()));

        given(userDetailsService.getUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails("1", "hmcts-admin@test.com",
                "Hmcts", "Test", UserRole.HMCTS_ADMIN.getRoles()));

        given(c2UploadedEmailContentProvider.buildC2UploadNotification(callbackRequest().getCaseDetails()))
            .willReturn(parameters);

        emailNotificationService.sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId(),
            "hmcts-admin@test.com", parameters, "12345");

        verify(emailNotificationService, times(0)).sendNotification(
            C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId(),
                "hmcts-admin@test.com", parameters, "12345");

        doNothing().when(emailNotificationService).sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId(),
            "hmcts-admin@test.com", parameters, "12345");

        notificationHandler.sendNotificationForC2Upload(new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        SendEmailResponse response = verify(notificationClient, times(0))
            .sendEmail(eq(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId()), eq("hmcts-admin@test.com"),
                eq(parameters), eq("12345"));

        assertThat(response).isNull();
    }

    @Test
    void shouldNotifyNonHmctsAdminOnC2Upload() throws IOException, NotificationClientException {
        final String SUBJ_LINE = "Lastname, SACCCCCCCC5676576567";
        final Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put("subjectLine", SUBJ_LINE)
            .put("hearingDetailsCallout", SUBJ_LINE)
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(userDetailsService.getUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails("1", "hmcts-non-admin@test.com",
                "Hmcts", "Test", UserRole.LOCAL_AUTHORITY.getRoles()));

        given(idamApi.retrieveUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails("1", "hmcts-non-admin@test.com",
                "Hmcts", "Test", UserRole.LOCAL_AUTHORITY.getRoles()));

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, "hmcts-non-admin@test.com"));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(c2UploadedEmailContentProvider.buildC2UploadNotification(callbackRequest().getCaseDetails()))
            .willReturn(parameters);

        doNothing().when(emailNotificationService).sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId(),
            "hmcts-non-admin@test.com", parameters, "12345");

        notificationHandler.sendNotificationForC2Upload(new C2UploadedEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId()), eq("hmcts-non-admin@test.com"),
            eq(parameters), eq("12345"));
    }

    @Test
    void shouldNotifyPartiesOnC21OrderSubmission() throws IOException, NotificationClientException {
        final Map<String, Object> parameters = ImmutableMap.<String, Object>builder()
            .put("court", COURT_NAME)
            .put("lastNameOfRespondent", "Test Lastname")
            .put("familyManCaseNumber", "SACCCCCCCC5676576567")
            .put("hearingDate", dateFormatterService.formatLocalDateToString(LocalDate.now().plusMonths(4),
                FormatStyle.MEDIUM))
            .build();

        given(userDetailsService.getUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails("1", "hmcts-non-admin@test.com",
                "Hmcts", "Test", UserRole.LOCAL_AUTHORITY.getRoles()));

        given(hmctsCourtLookupConfiguration.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new Court(COURT_NAME, COURT_EMAIL_ADDRESS));

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(c21OrderEmailContentProvider.buildC21OrderNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(parameters);

        doNothing().when(emailNotificationService).sendNotification(C21_ORDER_NOTIFICATION_TEMPLATE.getTemplateId(),
            "hmcts-non-admin@test.com", parameters, "12345");

        notificationHandler.sendNotificationForC21Order(new C21OrderEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(C21_ORDER_NOTIFICATION_TEMPLATE.getTemplateId()), eq("hmcts-non-admin@test.com"),
            eq(parameters), eq("12345"));
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

        doNothing().when(emailNotificationService).sendNotification(HMCTS_COURT_SUBMISSION_TEMPLATE.getTemplateId(),
            COURT_EMAIL_ADDRESS, expectedParameters, "12345");

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE.getTemplateId()), eq(COURT_EMAIL_ADDRESS),
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

        doNothing().when(emailNotificationService).sendNotification(CAFCASS_SUBMISSION_TEMPLATE.getTemplateId(),
            CAFCASS_EMAIL_ADDRESS, expectedParameters, "12345");

        notificationHandler.sendNotificationToCafcass(new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(CAFCASS_SUBMISSION_TEMPLATE.getTemplateId()), eq(CAFCASS_EMAIL_ADDRESS),
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

        doNothing().when(emailNotificationService).sendNotification(GATEKEEPER_SUBMISSION_TEMPLATE.getTemplateId(),
            GATEKEEPER_EMAIL_ADDRESS, expectedParameters, "12345");

        notificationHandler.sendNotificationToGatekeeper(new NotifyGatekeeperEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE.getTemplateId()), eq(GATEKEEPER_EMAIL_ADDRESS),
            eq(expectedParameters), eq("12345"));
    }
}
