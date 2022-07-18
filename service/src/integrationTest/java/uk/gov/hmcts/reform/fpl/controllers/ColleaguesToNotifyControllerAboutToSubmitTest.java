package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
public class ColleaguesToNotifyControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final long CASE_ID = 1;

    private static final List<Element<Colleague>> COLLEAGUES = wrapElements(
        Colleague.builder()
            .email("solicitor@solicitors.uk")
            .fullName("Solicitor Colleague")
            .build());

    private static final RespondentSolicitor SOLICITOR_A = RespondentSolicitor.builder()
        .firstName("Solicitor")
        .lastName("One")
        .build();

    private static final RespondentSolicitor SOLICITOR_WITH_COLLEAGUES = RespondentSolicitor.builder()
        .firstName("Solicitor")
        .lastName("Two")
        .colleaguesToBeNotified(COLLEAGUES)
        .build();


    private static final Respondent RESPONDENT_ONE = Respondent.builder()
        .party(RespondentParty.builder().firstName("David").lastName("Jones").build())
        .solicitor(SOLICITOR_A)
        .build();

    private static final Respondent RESPONDENT_WITHOUT_SOLICITOR = Respondent.builder()
        .party(RespondentParty.builder().firstName("Harry").lastName("Harries").build())
        .build();

    private static final Respondent RESPONDENT_WITH_SOLICITOR_COLLEAGUES = Respondent.builder()
        .party(RespondentParty.builder().firstName("Alex").lastName("Smith").build())
        .solicitor(SOLICITOR_WITH_COLLEAGUES)
        .build();

    private static final Child CHILD_ONE_WITH_SOLICITOR = Child.builder()
        .party(ChildParty.builder().firstName("Bobby").lastName("Tables").build())
        .solicitor(SOLICITOR_A)
        .build();

    private static final Child CHILD_TWO_WITH_SOLICITOR = Child.builder()
        .party(ChildParty.builder().firstName("Robert").lastName("Tables").build())
        .solicitor(SOLICITOR_A)
        .build();

    private static final Child CHILD_WITHOUT_SOLICITOR = Child.builder()
        .party(ChildParty.builder().firstName("Reginald").lastName("Tables").build())
        .build();

    @MockBean
    private CaseRoleLookupService caseRoleLookupService;

    protected ColleaguesToNotifyControllerAboutToSubmitTest() {
        super("add-colleagues-to-notify");
    }

    @Test
    void shouldUpdateMultipleChildrenWhenRepresented() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.CHILDSOLICITORA, CaseRole.CHILDSOLICITORB);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of(SolicitorRole.CHILDSOLICITORA, SolicitorRole.CHILDSOLICITORB));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(CHILD_ONE_WITH_SOLICITOR, CHILD_TWO_WITH_SOLICITOR, CHILD_WITHOUT_SOLICITOR))
            .colleaguesToNotify(COLLEAGUES)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(callbackResponse.getErrors()).isNull();
        // Check both children have the right colleagues
        assertThat(after.getAllChildren().get(0).getValue().getSolicitor().getColleaguesToBeNotified())
            .isEqualTo(COLLEAGUES);
        assertThat(after.getAllChildren().get(1).getValue().getSolicitor().getColleaguesToBeNotified())
            .isEqualTo(COLLEAGUES);
        assertThat(after.getAllChildren().get(2).getValue()).isEqualTo(CHILD_WITHOUT_SOLICITOR); // should be unchanged
    }

    @Test
    void shouldUpdateRespondentWhenRepresented() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.SOLICITORA);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of(SolicitorRole.SOLICITORA));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(RESPONDENT_ONE, RESPONDENT_WITHOUT_SOLICITOR))
            .colleaguesToNotify(COLLEAGUES)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(after.getAllRespondents().get(0).getValue().getSolicitor().getColleaguesToBeNotified())
            .isEqualTo(COLLEAGUES);
        assertThat(after.getAllRespondents().get(1).getValue()).isEqualTo(RESPONDENT_WITHOUT_SOLICITOR); // unchanged
    }

    @Test
    void shouldClearTemporaryFields() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.CHILDSOLICITORA);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of(SolicitorRole.CHILDSOLICITORA));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(wrapElements(CHILD_ONE_WITH_SOLICITOR))
            .colleaguesToNotify(COLLEAGUES)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(after.getColleaguesToNotify()).isEmpty();
        assertThat(callbackResponse.getData().get("respondentName")).isNull();
    }

    @Test
    void shouldThrowErrorIfNotRepresentingAnyone() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.SOLICITORB);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of());

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(RESPONDENT_ONE))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains("There is no one this user is representing on this case.");
    }
}
