package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerRepresentationValidationMidEventTest extends AbstractCallbackTest {

    private static final String SOLICITOR_ROLE = "caseworker-publiclaw-solicitor";
    private static final String ADMIN_ROLE = "caseworker-publiclaw-courtadmin";

    private static final Child CHILD_1 = Child.builder().party(ChildParty.builder().firstName("dave").build()).build();
    private static final Child CHILD_2 = Child.builder().party(ChildParty.builder().firstName("jack").build()).build();
    private static final List<Element<Child>> CHILDREN = wrapElements(CHILD_1, CHILD_2);
    private static final ChildRepresentationDetails USE_MAIN = ChildRepresentationDetails.builder()
        .useMainSolicitor("Yes")
        .build();
    private static final ChildRepresentationDetails USE_REGISTERED_ORG = ChildRepresentationDetails.builder()
        .useMainSolicitor("No")
        .solicitor(RespondentSolicitor.builder()
            .firstName("jeff")
            .lastName("wayne")
            .organisation(Organisation.builder().organisationID("thunder child").build())
            .email("the-war@of-the.worlds")
            .build())
        .build();

    ChildControllerRepresentationValidationMidEventTest() {
        super("enter-children");
    }

    @Test
    void shouldReturnErrorsWhenSolicitorUpdatesAlreadySetRepresentative() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("Yes")
                .childRepresentationDetails0(USE_MAIN)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(USE_REGISTERED_ORG)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-validation", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot change a child's legal representative"));
    }

     @Test
    void shouldReturnNoErrorsWhenSolicitorSetsRepresentativeForTheFirstTime() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(USE_REGISTERED_ORG)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-validation", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenAdminUpdatesAlreadySetRepresentative() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_REGISTERED_ORG)
                .childRepresentationDetails1(USE_MAIN)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(USE_REGISTERED_ORG)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-validation", ADMIN_ROLE
        );

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenMandatoryFieldsAreNotPopulated() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(RespondentSolicitor.builder().organisation(Organisation.builder().build()).build())
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-validation");

        assertThat(response.getErrors()).isEqualTo(List.of(
            "Add the full name of jack's legal representative",
            "Add the email address of jack's legal representative",
            "Add the organisation details for jack's legal representative"
        ));
    }

    @Test
    void shouldReturnErrorsWhenEmailsAreNotValid() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("jeff")
                        .lastName("wayne")
                        .organisation(Organisation.builder().organisationID("thunder child").build())
                        .email("the war of the worlds")
                        .build())
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-validation");

        assertThat(response.getErrors()).isEqualTo(List.of(
            "Enter a correct email address for jack's legal representative, for example name@example.com"
        ));
    }

    @Test
    void shouldReturnNoErrors() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(USE_MAIN)
                .childRepresentationDetails1(USE_REGISTERED_ORG)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-validation");

        assertThat(response.getErrors()).isEmpty();
    }
}
