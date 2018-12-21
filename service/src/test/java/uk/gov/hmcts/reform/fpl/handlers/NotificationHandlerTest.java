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
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
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

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Test
    void shouldSendEmailToHmcts() throws IOException, NotificationClientException {
        final Map<String, String> expectedParameters = ImmutableMap.<String, String>builder()
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
        final Map<String, String> expectedParameters = ImmutableMap.<String, String>builder()
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
        final Map<String, String> expectedParameters = ImmutableMap.<String, String>builder()
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
}
