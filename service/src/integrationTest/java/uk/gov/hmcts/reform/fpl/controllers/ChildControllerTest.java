package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

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
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of("data", "some data"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(request);

        assertThat(callbackResponse.getData()).containsKey("children1");
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenFutureDateOfBirth() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(LocalDate.now().plusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenThereIsMultipleChildren() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of(
                    "children1", ImmutableList.of(
                        createChildrenElement(LocalDate.now().plusDays(1)),
                        createChildrenElement(LocalDate.now().plusDays(1))
                    )))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorWhenValidDateOfBirth() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of("children1", ImmutableList.of(
                    createChildrenElement(LocalDate.now().minusDays(1)))))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsWhenCaseDataIsEmpty() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(ImmutableMap.of())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(request);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldAddConfidentialChildrenToCaseDataWhenConfidentialChildrenExist() throws Exception {
        //first child in callbackRequest() has yes value for detailsHidden.
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest());
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseData initialData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        assertThat(caseData.getConfidentialChildren()).containsOnly(initialData.getAllChildren().get(0));
        assertThat(caseData.getChildren1().get(0).getValue().getParty().address).isNull();
        assertThat(caseData.getChildren1().get(1).getValue().getParty().address).isNotNull();
    }

    private Map<String, Object> createChildrenElement(LocalDate dateOfBirth) {
        return ImmutableMap.of(
            "id", "",
            "value", Child.builder()
                .party(ChildParty.builder()
                    .dateOfBirth(dateOfBirth)
                    .build())
                .build());
    }
}
