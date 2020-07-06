package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PartyAddedToCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
    private NotificationService notificationService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private PartyAddedToCaseContentProvider partyAddedToCaseContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartyAddedToCaseEventHandler partyAddedToCaseEventHandler;

    private static CaseDetails caseDetails = callbackRequest().getCaseDetails();
    private static CaseDetails caseDetailsBefore = callbackRequest().getCaseDetailsBefore();
    private static CaseData caseData;
    private static CaseData caseDataBefore;

    @BeforeEach
    void init() {
        caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        caseDataBefore = objectMapper.convertValue(caseDetailsBefore.getData(), CaseData.class);

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
    }

    @Test
    void shouldSendEmailToPartiesWhenAddedToCase() {
        final Map<String, Object> expectedEmailParameters = getPartyAddedByEmailNotificationParameters();
        final Map<String, Object> expectedDigitalParameters = getPartyAddedByDigitalServiceNotificationParameters();

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), EMAIL)).willReturn(expectedEmailParameters);

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), DIGITAL_SERVICE)).willReturn(expectedDigitalParameters);

        partyAddedToCaseEventHandler.sendEmailToPartiesAddedToCase(
            new PartyAddedToCaseEvent(callbackRequest()));

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
    void shouldNotSendEmailToPartiesWhichHaveNotBeenUpdated() {
        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), DIGITAL_SERVICE)).willReturn(getUpdatedRepresentatives());

        given(representativeService.getUpdatedRepresentatives(caseData.getRepresentatives(),
            caseDataBefore.getRepresentatives(), EMAIL)).willReturn(Collections.emptyList());

        given(partyAddedToCaseContentProvider.getPartyAddedToCaseNotificationParameters(
            callbackRequest().getCaseDetails(), DIGITAL_SERVICE))
            .willReturn(getPartyAddedByDigitalServiceNotificationParameters());

        partyAddedToCaseEventHandler.sendEmailToPartiesAddedToCase(
            new PartyAddedToCaseEvent(callbackRequest()));

        verify(notificationService, never()).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE),
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL),
            anyMap(),
            eq("12345"));

        verify(notificationService, never()).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE),
            eq(PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS),
            anyMap(),
            eq("12345"));

        verify(notificationService).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE),
            eq("johnmoley@test.com"),
            eq(getPartyAddedByDigitalServiceNotificationParameters()),
            eq("12345"));
    }

    private List<Representative> getUpdatedRepresentatives() {
        return List.of(Representative.builder()
            .fullName("John Moley")
            .email("johnmoley@test.com")
            .servingPreferences(DIGITAL_SERVICE)
            .address(Address.builder()
                .addressLine1("A1")
                .postcode("CR0 2GE")
                .build())
            .build());
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
