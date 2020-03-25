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

import java.time.LocalDate;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerTest extends AbstractControllerTest {

    ChildControllerTest() {
        super("enter-children");
    }

    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Test
    void aboutToStartShouldPrepopulateChildrenDataWhenNoChildExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("children1");
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenFutureDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", wrapElements(createChildWithDateOfBirth(LocalDate.now().plusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenThereIsMultipleChildren() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", wrapElements(
                createChildWithDateOfBirth(LocalDate.now().plusDays(1)),
                createChildWithDateOfBirth(LocalDate.now().plusDays(1))
            )))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorWhenValidDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", wrapElements(createChildWithDateOfBirth(LocalDate.now().minusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsWhenCaseDataIsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(emptyMap())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldAddConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() throws Exception {
        //first child in callbackRequest() has yes value for detailsHidden.
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest());
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseData initialData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        assertThat(caseData.getConfidentialChildren())
            .containsOnly(retainConfidentialDetails(initialData.getAllChildren().get(0)));

        assertThat(caseData.getChildren1().get(0).getValue().getParty().address).isNull();
        assertThat(caseData.getChildren1().get(1).getValue().getParty().address).isNotNull();
    }

    private Child createChildWithDateOfBirth(LocalDate date) {
        return Child.builder()
            .party(ChildParty.builder().dateOfBirth(date).build())
            .build();
    }

    private Element<Child> retainConfidentialDetails(Element<Child> child) {
        return element(child.getId(), Child.builder()
            .party(ChildParty.builder()
                .firstName(child.getValue().getParty().firstName)
                .lastName(child.getValue().getParty().lastName)
                .address(child.getValue().getParty().address)
                .telephoneNumber(child.getValue().getParty().telephoneNumber)
                .email(child.getValue().getParty().email)
                .build())
            .build());
    }
}
