package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.HmctsCourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
class NotificationHandlerTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String COURT_NAME = "Test court";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";

    @Mock
    private HmctsCourtLookUpService hmctsCourtLookUpService;

    @Mock
    private LocalAuthorityService localAuthorityService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Test
    void shouldSendEmail() throws IOException, NotificationClientException {
        CallbackRequest request = callbackRequest();
        final Map<String, String> expectedParameters = ImmutableMap.<String, String>builder()
            .put("court", COURT_NAME)
            .put("localAuthority", "Example Local Authority")
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

        given(hmctsCourtLookUpService.getCourt(LOCAL_AUTHORITY_CODE))
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityService.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            eq(HMCTS_COURT_SUBMISSION_TEMPLATE), eq(COURT_EMAIL_ADDRESS), eq(expectedParameters), eq("12345"));
    }
}
