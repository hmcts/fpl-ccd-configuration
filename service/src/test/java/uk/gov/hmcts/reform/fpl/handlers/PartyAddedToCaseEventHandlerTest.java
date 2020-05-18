package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedDigitalRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getExpectedEmailRepresentativesForAddingPartiesToCase;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PartyAddedToCaseEventHandler.class, LookupTestConfig.class, JacksonAutoConfiguration.class,
    RepresentativeNotificationService.class})
public class PartyAddedToCaseEventHandlerTest {
    @MockBean
    private RequestData requestData;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartyAddedToCaseEventHandler partyAddedToCaseEventHandler;

    @Test
    void shouldSendEmailToPartiesWhenAddedToCase() {
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

        partyAddedToCaseEventHandler.sendEmailToPartiesAddedToCase(
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
}
