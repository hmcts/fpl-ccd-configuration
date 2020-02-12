package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
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
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @MockBean
    private NotificationClient notificationClient;

    private static final Long CASE_ID = 12345L;
    private static final String CASE_REFERENCE = "12345";
    private static final String RESPONDENT_SURNAME = "Watson";

    RepresentativeSubmittedEventControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldSendNotificationWhenNewPartyIsAddedOrUpdatedToCaseByEmail() throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(EMAIL);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE), eq("test@test.com"),
            eq(expectedTemplateParametersEmail()), eq(CASE_REFERENCE));
    }


    @Test
    void shouldSendNotificationWhenNewPartyIsAddedOrUpdatedToCaseThroughDigitalService()
        throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(DIGITAL_SERVICE);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient).sendEmail(
            eq(PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE), eq("test@test.com"),
            eq(expectedTemplateParametersDigitalService()), eq(CASE_REFERENCE));
    }

    @Test
    void shouldNotSendNotificationWhenNoPartiesAreUpdatedOrAddedToCase() throws NotificationClientException {
        final UUID representativeId = UUID.randomUUID();

        Respondent respondent = buildRespondent();

        Representative representative = buildRepresentative(POST);

        CaseDetails caseDetailsBefore = buildCaseData(respondent, emptyList());
        CaseDetails caseDetails = buildCaseData(respondent, List.of(element(representativeId, representative)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never())
            .sendEmail(eq(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE), eq("test@test.com"),
                eq(expectedTemplateParametersEmail()), eq(CASE_REFERENCE));
    }

    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private Representative buildRepresentative(RepresentativeServingPreferences servingPreference) {
        return Representative.builder()
            .fullName("John Smith")
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
            "caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345"
        );
    }

    private static CaseDetails buildCaseData(Respondent respondent, List<Element<Representative>> representatives) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "representatives", representatives,
                "respondents1", wrapElements(respondent)))
            .build();
    }
}
