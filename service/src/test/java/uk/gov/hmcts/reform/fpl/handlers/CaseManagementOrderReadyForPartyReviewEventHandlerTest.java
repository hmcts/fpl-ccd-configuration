package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForPartyReviewEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static com.microsoft.applicationinsights.boot.dependencies.google.common.base.Charsets.ISO_8859_1;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedDigitalRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedEmailRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderReadyForPartyReviewEventHandler.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, RepresentativeNotificationService.class, HearingBookingService.class,
    FixedTimeConfiguration.class})
public class CaseManagementOrderReadyForPartyReviewEventHandlerTest {

    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseManagementOrderReadyForPartyReviewEventHandler orderReadyForPartyReviewEventHandler;

    @Test
    void shouldNotifyRepresentativesOfCMOReadyForPartyReview() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalRepresentativesForAddingPartiesToCase());

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(caseManagementOrderEmailContentProvider.buildCMOPartyReviewParameters(caseDetails, DOCUMENT_CONTENTS,
            DIGITAL_SERVICE))
            .willReturn((getCMOReadyforReviewByPartiesNotificationParameters(DIGITAL_SERVICE)));

        given(caseManagementOrderEmailContentProvider.buildCMOPartyReviewParameters(caseDetails, DOCUMENT_CONTENTS,
            EMAIL))
            .willReturn((getCMOReadyforReviewByPartiesNotificationParameters(EMAIL)));

        orderReadyForPartyReviewEventHandler.sendEmailForCaseManagementOrderReadyForPartyReview(
            new CaseManagementOrderReadyForPartyReviewEvent(callbackRequest, requestData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("fred@flinstone.com"),
            eqJson(getCMOReadyforReviewByPartiesNotificationParameters(DIGITAL_SERVICE)),
            eq("12345"));

        verify(notificationService).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("barney@rubble.com"),
            eqJson(getCMOReadyforReviewByPartiesNotificationParameters(EMAIL)),
            eq("12345"));
    }

    private Map<String, Object> getCMOReadyforReviewByPartiesNotificationParameters(
        RepresentativeServingPreferences servingPreference) {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENTS), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        final String subjectLine = "Jones, SACCCCCCCC5676576567," + " hearing 1 Feb 2020";

        return ImmutableMap.<String, Object>builder()
            .put("subjectLineWithHearingDate", subjectLine)
            .put("respondentLastName", "Jones")
            .put("digitalPreference", servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .put("caseUrl", servingPreference == DIGITAL_SERVICE ? formatCaseUrl("http://fake-url", 12345L) : "")
            .put("link_to_document", jsonFileObject)
            .build();
    }
}
