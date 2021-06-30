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
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerRepresentationDetailsMidEventTest extends AbstractCallbackTest {

    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .lastName("Jeff")
        .lastName("Wayne")
        .email("the-eve@of-the.war")
        .organisation(Organisation.builder()
            .organisationID("7ligZljXfUtcKPCotWul5g")
            .build())
        .build();

    private static final RespondentSolicitor OTHER_REPRESENTATIVE = RespondentSolicitor.builder()
        .lastName("Richard")
        .lastName("Burton")
        .email("horsell-common@and-the-heat.ray")
        .organisation(Organisation.builder()
            .organisationID("7ligZljXfUtcKPCotWul5g")
            .build())
        .build();

    ChildControllerRepresentationDetailsMidEventTest() {
        super("enter-children");
    }

    @Test
    void shouldReturnErrorsIfMainSolicitorHasInvalidEmail() {
        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE.toBuilder().email("the eve of the war").build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");

        assertThat(response.getErrors()).isEqualTo(List.of(
            "Enter an email address in the correct format for the children's main legal representative, for example "
            + "name@example.com"
        ));
    }

    @Test
    void shouldPopulateChildrenRepresentationDetails() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");
        CaseData responseData = extractCaseData(response);

        assertThat(response.getData().get("optionCount")).isEqualTo("0");
        assertThat(responseData.getChildrenEventData()).isEqualTo(ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenMainRepresentative(MAIN_REPRESENTATIVE)
            .childRepresentationDetails0(ChildRepresentationDetails.builder()
                .childDescription("Child 1 - Justin Hayward")
                .build())
            .build()
        );
    }

    @Test
    void shouldPullMainSolicitorDetailsToTheRequiredChildrenRepresentationDetails() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .representative(MAIN_REPRESENTATIVE)
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .childrenHaveSameRepresentation("No")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");
        CaseData responseData = extractCaseData(response);

        assertThat(response.getData().get("optionCount")).isEqualTo("0");
        assertThat(responseData.getChildrenEventData()).isEqualTo(ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenMainRepresentative(MAIN_REPRESENTATIVE)
            .childrenHaveSameRepresentation("No")
            .childRepresentationDetails0(ChildRepresentationDetails.builder()
                .childDescription("Child 1 - Justin Hayward")
                .useMainSolicitor("Yes")
                .build())
            .build()
        );
    }

    @Test
    void shouldPullExistingSolicitorDetailsToTheRequiredChildrenRepresentationDetails() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .representative(OTHER_REPRESENTATIVE)
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .childrenHaveSameRepresentation("No")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");
        CaseData responseData = extractCaseData(response);

        assertThat(response.getData().get("optionCount")).isEqualTo("0");
        assertThat(responseData.getChildrenEventData()).isEqualTo(ChildrenEventData.builder()
            .childrenHaveRepresentation("Yes")
            .childrenMainRepresentative(MAIN_REPRESENTATIVE)
            .childrenHaveSameRepresentation("No")
            .childRepresentationDetails0(ChildRepresentationDetails.builder()
                .childDescription("Child 1 - Justin Hayward")
                .useMainSolicitor("No")
                .solicitor(OTHER_REPRESENTATIVE)
                .build())
            .build()
        );
    }

    @Test
    void shouldNotUseExistingSolicitorDetailsWhenNoMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .representative(OTHER_REPRESENTATIVE)
                .build()))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");
        CaseData responseData = extractCaseData(response);

        assertThat(response.getData().get("optionCount")).isNull();
        assertThat(responseData.getChildrenEventData()).isEqualTo(ChildrenEventData.builder()
            .childrenHaveRepresentation("No")
            .build()
        );
    }
}
