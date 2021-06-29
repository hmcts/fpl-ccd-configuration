package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerRepresentationValidationMidEventTest extends AbstractCallbackTest {

    ChildControllerRepresentationValidationMidEventTest() {
        super("enter-children");
    }

    @Test
    void shouldReturnErrorsWhenMandatoryFieldsAreNotPopulated() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder().build(), Child.builder().build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("Yes")
                    .build())
                .childRepresentationDetails1(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(Organisation.builder().build())
                        .build())
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-validation");

        assertThat(response.getErrors()).isEqualTo(List.of(
            "Add the full name of child 2's legal representative",
            "Add the email address of child 2's legal representative",
            "Add the organisation details for child 2's legal representative"
        ));
    }

    @Test
    void shouldReturnErrorsWhenEmailsAreNotValid() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder().build(), Child.builder().build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("Yes")
                    .build())
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
            "Enter an email address in the correct format for child 2's legal representative, for example "
            + "name@example.com"
        ));
    }

    @Test
    void shouldReturnNoErrors() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder().build(), Child.builder().build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenMainRepresentative(RespondentSolicitor.builder().build())
                .childrenHaveRepresentation("Yes")
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("Yes")
                    .build())
                .childRepresentationDetails1(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("jeff")
                        .lastName("wayne")
                        .organisation(Organisation.builder().organisationID("thunder child").build())
                        .email("the-war@of-the.worlds")
                        .build())
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-validation");

        assertThat(response.getErrors()).isEmpty();
    }

}
