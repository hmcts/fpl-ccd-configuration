package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class RepresentativeSubmittedEventControllerTest extends AbstractCallbackTest {

    private static final String REPRESENTATIVE_FULLNAME = "John Smith";
    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final SyntheticCaseSummary CASE_SUMMARY = SyntheticCaseSummary.builder()
        .caseSummaryFirstRespondentLegalRep(REPRESENTATIVE_FULLNAME)
        .caseSummaryFirstRespondentLastName(RESPONDENT_SURNAME)
        .caseSummaryCourtName(COURT_NAME)
        .caseSummaryNumberOfChildren(1)
        .build();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final String CHILD_LAST_NAME = "something";
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    RepresentativeSubmittedEventControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldSendNotificationWithEmailPreference() throws NotificationClientException {
        CaseDetails caseDetailsBefore = buildCaseData(emptyList(), emptyMap());
        CaseDetails caseDetails = buildCaseData(wrapElements(buildRepresentative(EMAIL)), emptyMap());

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verify(notificationClient).sendEmail(
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE, "test@test.com",
            expectedTemplateParametersEmail(), NOTIFICATION_REFERENCE
        );

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION, CASE_TYPE, CASE_ID, "internal-update-case-summary", caseSummary()
        );
    }


    @Test
    void shouldSendNotificationWithDigitalPreference() throws NotificationClientException {
        CaseDetails caseDetailsBefore = buildCaseData(emptyList(), emptyMap());
        CaseDetails caseDetails = buildCaseData(wrapElements(buildRepresentative(DIGITAL_SERVICE)), emptyMap());

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verify(notificationClient).sendEmail(
            PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE, "test@test.com",
            expectedTemplateParametersDigitalService(), NOTIFICATION_REFERENCE
        );
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION, CASE_TYPE, CASE_ID, "internal-update-case-summary", caseSummary()
        );
    }

    @Test
    void shouldNotSendNotificationWhenNoPartiesAreUpdatedOrAddedToCase() {
        List<Element<Representative>> representative = wrapElements(buildRepresentative(DIGITAL_SERVICE));
        CaseDetails caseDetailsBefore = buildCaseData(representative, caseSummary());
        CaseDetails caseDetails = buildCaseData(representative, caseSummary());

        postSubmittedEvent(toCallBackRequest(caseDetails, caseDetailsBefore));

        verifyNoInteractions(notificationClient, coreCaseDataService);
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

    private Map<String, Object> expectedTemplateParametersEmail() {
        return Map.of(
            "familyManCaseNumber", "",
            "firstRespondentLastName", RESPONDENT_SURNAME,
            "childLastName", CHILD_LAST_NAME
        );
    }

    private Map<String, Object> expectedTemplateParametersDigitalService() {
        return Map.of(
            "familyManCaseNumber", "",
            "firstRespondentLastName", RESPONDENT_SURNAME,
            "caseUrl", "http://fake-url/cases/case-details/12345",
            "childLastName", CHILD_LAST_NAME
        );
    }

    private CaseDetails buildCaseData(List<Element<Representative>> representatives,
                                      Map<String, Object> caseSummary) {
        Map<String, Object> data = new HashMap<>();
        data.put("representatives", representatives);
        data.put("respondents1", wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName(RESPONDENT_SURNAME).build())
            .build()));
        data.put("children1", wrapElements(Child.builder()
            .party(ChildParty.builder().dateOfBirth(dateNow()).lastName(CHILD_LAST_NAME).build())
            .build()
        ));
        data.put("caseLocalAuthority", LOCAL_AUTHORITY_1_CODE);
        data.putAll(caseSummary);
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(data)
            .build();
    }

    private Map<String, Object> caseSummary() {
        return mapper.convertValue(CASE_SUMMARY, MAP_TYPE);
    }
}
