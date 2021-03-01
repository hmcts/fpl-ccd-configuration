package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerTest extends AbstractControllerTest {

    ChildControllerTest() {
        super("enter-children");
    }

    @Test
    void aboutToStartShouldPrepopulateChildrenDataWhenNoChildExists() {
        CaseData caseData = CaseData.builder().build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);

        assertThat(callbackResponse.getData()).containsKey("children1");
    }

    @Test
    void aboutToSubmitShouldAddConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() {
        //first child in callbackRequest() has yes value for detailsHidden.
        CaseData initialCaseData = caseData();

        CaseData caseData = extractCaseData(postAboutToSubmitEvent(initialCaseData));

        assertThat(caseData.getConfidentialChildren())
            .containsOnly(retainConfidentialDetails(initialCaseData.getAllChildren().get(0)));

        assertThat(caseData.getChildren1().get(0).getValue().getParty().getAddress()).isNull();
        assertThat(caseData.getChildren1().get(1).getValue().getParty().getAddress()).isNotNull();
    }

    private Element<Child> retainConfidentialDetails(Element<Child> child) {
        return element(child.getId(), Child.builder()
            .party(ChildParty.builder()
                .firstName(child.getValue().getParty().getFirstName())
                .lastName(child.getValue().getParty().getLastName())
                .address(child.getValue().getParty().getAddress())
                .telephoneNumber(child.getValue().getParty().getTelephoneNumber())
                .email(child.getValue().getParty().getEmail())
                .showAddressInConfidentialTab("Yes")
                .build())
            .build());
    }
}
