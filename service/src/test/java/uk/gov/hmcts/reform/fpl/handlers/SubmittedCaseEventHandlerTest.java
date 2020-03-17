package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
public class SubmittedCaseEventHandlerTest {
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String COURT_CODE = "11";
    private static final String COURT_NAME = "Test court";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String CTSC_INBOX = "Ctsc+test@gmail.com";
    private static final String CAFCASS_EMAIL_ADDRESS = "FamilyPublicLaw+cafcass@gmail.com";
    private static final String CAFCASS_NAME = "cafcass";

    @Mock
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

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
            .willReturn(new HmctsCourtLookupConfiguration.Court(COURT_NAME, COURT_EMAIL_ADDRESS, COURT_CODE));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(hmctsEmailContentProvider.buildHmctsSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        submittedCaseEventHandler.sendEmailToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

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

        submittedCaseEventHandler.sendEmailToHmctsAdmin(
            new SubmittedCaseEvent(callbackRequest, AUTH_TOKEN, USER_ID));

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
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        given(cafcassEmailContentProvider.buildCafcassSubmissionNotification(callbackRequest().getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).willReturn(expectedParameters);

        submittedCaseEventHandler.sendEmailToCafcass(
            new SubmittedCaseEvent(callbackRequest(), AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE, CAFCASS_EMAIL_ADDRESS,
            expectedParameters, "12345");
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
