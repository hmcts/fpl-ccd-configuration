package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalRepresentativeSubmitControllerTest extends AbstractCallbackTest {

    private static final String REPRESENTATIVE_EMAIL = "test@test.com";
    private static final String FAMILY_MAN_CASE_NO = "2313213132132";
    private static final String REP_NAME = "John Smith";
    private static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName(REP_NAME)
        .role(LegalRepresentativeRole.EXTERNAL_LA_BARRISTER)
        .email(REPRESENTATIVE_EMAIL)
        .organisation("organisation")
        .telephoneNumber("07500045455")
        .build();

    private static final Long CASE_ID = 12345L;
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final Respondent RESPONDENT = Respondent.builder().party(RespondentParty.builder()
        .lastName(RESPONDENT_SURNAME).build())
        .build();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private NotificationClient notificationClient;

    ManageLegalRepresentativeSubmitControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldSendNotificationWhenLegalRepresentativeAddedToCase() throws NotificationClientException {

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verify(notificationClient)
            .sendEmail(LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE, REPRESENTATIVE_EMAIL,
                expectedTemplateParameters(), NOTIFICATION_REFERENCE);
    }

    @Test
    void shouldNotSendNotificationWhenLegalRepresentativeDidNotChange() {

        CaseDetails caseDetailsBefore = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postSubmittedEvent(callbackRequest);

        verifyNoInteractions(notificationClient);
    }

    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private Map<String, Object> expectedTemplateParameters() {
        return ImmutableMap.of(
            "repName", REP_NAME,
            "localAuthority", LOCAL_AUTHORITY_1_NAME,
            "firstRespondentLastName", RESPONDENT_SURNAME,
            "familyManCaseNumber", FAMILY_MAN_CASE_NO,
            "caseUrl", "http://fake-url/cases/case-details/12345"
        );
    }


    private CaseDetails buildCaseData(List<Element<LegalRepresentative>> legalRepresentatives) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "familyManCaseNumber", FAMILY_MAN_CASE_NO,
                "legalRepresentatives", legalRepresentatives,
                "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
                "respondents1", wrapElements(RESPONDENT))
            )
            .build();
    }
}
