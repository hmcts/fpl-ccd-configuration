package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class RepresentativeSubmittedEventControllerTest extends AbstractControllerTest {

    private static final String REPRESENTATIVE_FULLNAME = "John Smith";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final SyntheticCaseSummary CASE_SUMMARY = SyntheticCaseSummary.builder()
        .caseSummaryFirstRespondentLegalRep(REPRESENTATIVE_FULLNAME)
        .caseSummaryFirstRespondentLastName(RESPONDENT_SURNAME)
        .build();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    RepresentativeSubmittedEventControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldSendNotificationWhenNewPartyIsAddedOrUpdatedToCaseByEmail() throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(EMAIL);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList(), emptyMap());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)),
            emptyMap());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE, "test@test.com",
            expectedTemplateParametersEmail(), NOTIFICATION_REFERENCE);

        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary());
    }


    @Test
    void shouldSendNotificationWhenNewPartyIsAddedOrUpdatedToCaseThroughDigitalService()
        throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(DIGITAL_SERVICE);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList(), emptyMap());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)),
            emptyMap());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE, "test@test.com",
            expectedTemplateParametersDigitalService(), NOTIFICATION_REFERENCE);
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary());
    }

    @Test
    void shouldNotSendNotificationWhenNoPartiesAreUpdatedOrAddedToCase() throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(POST);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList(), caseSummary());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)),
            caseSummary());

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verifyNoInteractions(notificationClient, coreCaseDataService);
    }


    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private Representative buildRepresentative(RepresentativeServingPreferences servingPreference) {
        return Representative.builder()
            .fullName(REPRESENTATIVE_FULLNAME)
            .positionInACase("Position")
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .servingPreferences(servingPreference)
            .email("test@test.com")
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .build();
    }

    private Respondent buildRespondent() {
        return Respondent.builder().party(RespondentParty.builder()
            .lastName(RESPONDENT_SURNAME).build())
            .build();
    }

    private Map<String, Object> expectedTemplateParametersEmail() {
        return ImmutableMap.of(
            "familyManCaseNumber", "",
            "firstRespondentLastName", RESPONDENT_SURNAME
        );
    }

    private Map<String, Object> expectedTemplateParametersDigitalService() {
        return ImmutableMap.of(
            "familyManCaseNumber", "",
            "firstRespondentLastName", RESPONDENT_SURNAME,
            "caseUrl", "http://fake-url/cases/case-details/12345"
        );
    }

    private CaseDetails buildCaseData(Respondent respondent, List<Element<Representative>> representatives,
                                      Map<String, Object> caseSummary) {
        Map<String, Object> data = new HashMap<>();
        data.put("representatives", representatives);
        data.put("respondents1", wrapElements(respondent));
        data.putAll(caseSummary);
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(data)
            .build();
    }

    private Map<String, Object> caseSummary() {
        return objectMapper.convertValue(
            CASE_SUMMARY, new TypeReference<>() {});
    }
}
