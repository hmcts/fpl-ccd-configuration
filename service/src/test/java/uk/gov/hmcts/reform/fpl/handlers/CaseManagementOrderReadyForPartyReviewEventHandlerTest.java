package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForPartyReviewEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CmoNotifyData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static com.google.common.base.Charsets.ISO_8859_1;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.DOCUMENT_CONTENTS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedDigitalRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedEmailRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderReadyForPartyReviewEventHandler.class, LookupTestConfig.class,
    RepresentativeNotificationService.class, FixedTimeConfiguration.class})
class CaseManagementOrderReadyForPartyReviewEventHandlerTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Autowired
    private CaseManagementOrderReadyForPartyReviewEventHandler orderReadyForPartyReviewEventHandler;

    @Test
    void shouldNotifyRepresentativesOfCMOReadyForPartyReview() {
        CaseData caseData = caseData();

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            DIGITAL_SERVICE))
            .willReturn(getExpectedDigitalRepresentativesForAddingPartiesToCase());

        given(representativeService.getRepresentativesByServedPreference(caseData.getRepresentatives(),
            EMAIL))
            .willReturn(getExpectedEmailRepresentativesForAddingPartiesToCase());

        given(caseManagementOrderEmailContentProvider.buildCMOPartyReviewParameters(caseData, DOCUMENT_CONTENTS,
            DIGITAL_SERVICE))
            .willReturn((getCMOReadyforReviewByPartiesNotificationParameters(DIGITAL_SERVICE)));

        given(caseManagementOrderEmailContentProvider.buildCMOPartyReviewParameters(caseData, DOCUMENT_CONTENTS,
            EMAIL))
            .willReturn((getCMOReadyforReviewByPartiesNotificationParameters(EMAIL)));

        orderReadyForPartyReviewEventHandler.notifyRepresentatives(
            new CaseManagementOrderReadyForPartyReviewEvent(caseData, DOCUMENT_CONTENTS));

        verify(notificationService).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("fred@flinstone.com"),
            eqJson(getCMOReadyforReviewByPartiesNotificationParameters(DIGITAL_SERVICE)),
            eq(caseData.getId()));

        verify(notificationService).sendEmail(
            eq(CMO_READY_FOR_PARTY_REVIEW_NOTIFICATION_TEMPLATE),
            eq("barney@rubble.com"),
            eqJson(getCMOReadyforReviewByPartiesNotificationParameters(EMAIL)),
            eq(caseData.getId()));
    }

    private CmoNotifyData getCMOReadyforReviewByPartiesNotificationParameters(
        RepresentativeServingPreferences servingPreference) {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENTS), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        final String subjectLine = "Jones, SACCCCCCCC5676576567," + " hearing 1 Feb 2020";

        return CmoNotifyData.builder()
            .subjectLineWithHearingDate(subjectLine)
            .respondentLastName("Jones")
            .digitalPreference(servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .caseUrl(servingPreference == DIGITAL_SERVICE ? formatCaseUrl("http://fake-url", 12345L) : "")
            .documentLink(jsonFileObject.toMap())
            .build();
    }
}
