package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
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
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("children1");
    }

    @Test
    void aboutToSubmitShouldAddConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() {
        //first child in callbackRequest() has yes value for detailsHidden.
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest());
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseData initialData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        assertThat(caseData.getConfidentialChildren())
            .containsOnly(retainConfidentialDetails(initialData.getAllChildren().get(0)));

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
