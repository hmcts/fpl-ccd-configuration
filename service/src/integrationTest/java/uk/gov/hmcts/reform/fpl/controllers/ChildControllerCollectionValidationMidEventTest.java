package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerCollectionValidationMidEventTest extends AbstractCallbackTest {

    private static final UUID CHILD_1_ID = UUID.randomUUID();
    private static final UUID CHILD_2_ID = UUID.randomUUID();
    private static final Child CHILD_1 = Child.builder().party(ChildParty.builder().firstName("c1").build()).build();
    private static final Child CHILD_2 = Child.builder().party(ChildParty.builder().firstName("c2").build()).build();
    private static final Element<Child> CHILD_1_ELEMENT = element(CHILD_1_ID, CHILD_1);
    private static final Element<Child> CHILD_2_ELEMENT = element(CHILD_2_ID, CHILD_2);

    private static final String SOLICITOR_ROLE = "caseworker-publiclaw-solicitor";
    private static final String ADMIN_ROLE = "caseworker-publiclaw-courtadmin";

    ChildControllerCollectionValidationMidEventTest() {
        super("enter-children");
    }

    @DisplayName("Solicitor cannot add children to the case")
    @Test
    void solicitorAddition() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT, CHILD_2_ELEMENT))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot add a child to the case"));
    }

    @DisplayName("Solicitor cannot remove children from the case")
    @Test
    void solicitorRemoval() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot remove c1 from the case"));
    }

    @DisplayName("Solicitor can update the details of a child")
    @Test
    void solicitorUpdate() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(element(CHILD_1_ID, CHILD_2)))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEmpty();
    }

    @DisplayName("Admin can add children to the case")
    @Test
    void adminAddition() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT, CHILD_2_ELEMENT))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", ADMIN_ROLE
        );

        assertThat(response.getErrors()).isEmpty();
    }

    @DisplayName("Admin cannot remove children from the case once a representative is added")
    @Test
    void adminRemoval() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", ADMIN_ROLE
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot remove c1 from the case"));

    }

    @DisplayName("Admin can update the details of a child")
    @Test
    void adminUpdate() {
        CaseData caseData = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(element(CHILD_1_ID, CHILD_2)))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(State.SUBMITTED)
            .children1(List.of(CHILD_1_ELEMENT))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "validate-collection", ADMIN_ROLE
        );

        assertThat(response.getErrors()).isEmpty();
    }
}
