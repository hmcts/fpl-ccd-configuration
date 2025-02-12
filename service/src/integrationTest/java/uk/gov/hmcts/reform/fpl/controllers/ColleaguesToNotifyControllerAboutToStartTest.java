package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
public class ColleaguesToNotifyControllerAboutToStartTest extends AbstractCallbackTest {

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

    private static final Respondent RESPONDENT_WITH_SOLICITOR_COLLEAGUES = Respondent.builder()
        .party(RespondentParty.builder().firstName("Alex").lastName("Smith").build())
        .solicitor(SOLICITOR_WITH_COLLEAGUES)
        .build();

    @MockBean
    private CaseRoleLookupService caseRoleLookupService;

    protected ColleaguesToNotifyControllerAboutToStartTest() {
        super("add-colleagues-to-notify");
    }

    @Test
    void shouldSetRespondentNameIfTheyAreRepresented() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.SOLICITORA);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of(SolicitorRole.SOLICITORA));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(RESPONDENT_ONE))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getErrors()).isNull();
        assertThat(callbackResponse.getData()).extracting("respondentName").isEqualTo("David Jones");
    }

    @Test
    void shouldSetColleagesIfTheyHaveBeenAddedPreviously() {
        givenCaseRoles(CASE_ID, USER_ID, CaseRole.SOLICITORA);
        given(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(CASE_ID))
            .willReturn(List.of(SolicitorRole.SOLICITORA));

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(wrapElements(RESPONDENT_WITH_SOLICITOR_COLLEAGUES))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(callbackResponse.getErrors()).isNull();
        assertThat(after.getColleaguesToNotify()).isEqualTo(COLLEAGUES);
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

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains("There is no one this user is representing on this case.");
        assertThat(callbackResponse.getData()).doesNotContainKey("respondentName");
        assertThat(callbackResponse.getData()).extracting("colleaguesToNotify").isEqualTo(emptyList());
    }
}
