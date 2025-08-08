package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
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
    private static final String CHILD_SURNAME = "Holmes";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private UserService userService;

    ManageLegalRepresentativeSubmitControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldSendNotificationWhenLegalRepresentativeAddedToCase() throws NotificationClientException {
        CaseData caseDataBefore = buildCaseData(emptyList());
        CaseData caseData = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(notificationClient).sendEmail(
            LEGAL_REPRESENTATIVE_ADDED_TO_CASE_TEMPLATE,
            REPRESENTATIVE_EMAIL,
            expectedTemplateParameters(),
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldNotSendNotificationWhenLegalRepresentativeDidNotChange() {
        CaseData caseDataBefore = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));
        CaseData caseData = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verifyNoInteractions(notificationClient);
    }

    private Map<String, Object> expectedTemplateParameters() {
        return Map.of(
            "repName", REP_NAME,
            "localAuthority", LOCAL_AUTHORITY_1_NAME,
            "firstRespondentLastName", RESPONDENT_SURNAME,
            "familyManCaseNumber", FAMILY_MAN_CASE_NO,
            "caseUrl", caseUrl(CASE_ID),
            "childLastName", CHILD_SURNAME
        );
    }


    private CaseData buildCaseData(List<Element<LegalRepresentative>> legalRepresentatives) {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NO)
            .legalRepresentatives(legalRepresentatives)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName(RESPONDENT_SURNAME).build())
                .build()))
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().dateOfBirth(LocalDate.now()).lastName(CHILD_SURNAME).build())
                .build()))
            .build();
    }
}
