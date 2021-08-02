package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerRepresentationDetailsMidEventTest extends AbstractCallbackTest {

    private static final String SOLICITOR_ROLE = "caseworker-publiclaw-solicitor";
    private static final String ADMIN_ROLE = "caseworker-publiclaw-courtadmin";

    private static final RespondentSolicitor MAIN_REPRESENTATIVE = RespondentSolicitor.builder()
        .lastName("Jeff")
        .lastName("Wayne")
        .email("the-eve@of-the.war")
        .organisation(Organisation.builder()
            .organisationID("7ligZljXfUtcKPCotWul5g")
            .build())
        .unregisteredOrganisation(UnregisteredOrganisation.builder().address(Address.builder().build()).build())
        .regionalOfficeAddress(Address.builder().build())
        .build();

    private static final RespondentSolicitor OTHER_REPRESENTATIVE = RespondentSolicitor.builder()
        .lastName("Richard")
        .lastName("Burton")
        .email("horsell-common@and-the-heat.ray")
        .organisation(Organisation.builder()
            .organisationID("7ligZljXfUtcKPCotWul5g")
            .build())
        .unregisteredOrganisation(UnregisteredOrganisation.builder().address(Address.builder().build()).build())
        .regionalOfficeAddress(Address.builder().build())
        .build();

    ChildControllerRepresentationDetailsMidEventTest() {
        super("enter-children");
    }

    @ParameterizedTest(name = "with role {0}")
    @ValueSource(strings = {SOLICITOR_ROLE, ADMIN_ROLE})
    void shouldReturnErrorsIfRepresentativeIsSetFromYesToNo(String role) {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder().childrenHaveRepresentation("No").build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder().childrenHaveRepresentation("Yes").build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-details", role
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot remove the main representative from the case"));
    }

    @Test
    void shouldReturnErrorsIfRepresentativeIsUpdatedAndUserIsSolicitor() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(OTHER_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-details", SOLICITOR_ROLE
        );

        assertThat(response.getErrors()).isEqualTo(List.of("You cannot change the main representative"));
    }

    @Test
    void shouldNotReturnErrorsIfRepresentativeIsUpdatedAndUserIsAdmin() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(OTHER_REPRESENTATIVE)
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            toCallBackRequest(caseData, caseDataBefore), "representation-details", ADMIN_ROLE
        );

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldReturnErrorsIfMainSolicitorHasInvalidEmail() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(MAIN_REPRESENTATIVE.toBuilder().email("the eve of the war").build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "representation-details");

        assertThat(response.getErrors()).isEqualTo(List.of(
            "Enter a correct email address, for example name@example.com"
        ));
    }

    @Test
    void shouldPopulateChildrenRepresentationDetails() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
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
            .state(SUBMITTED)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .solicitor(MAIN_REPRESENTATIVE)
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
            .state(SUBMITTED)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .solicitor(OTHER_REPRESENTATIVE)
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
            .state(SUBMITTED)
            .children1(wrapElements(Child.builder()
                .party(ChildParty.builder().firstName("Justin").lastName("Hayward").build())
                .solicitor(OTHER_REPRESENTATIVE)
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
