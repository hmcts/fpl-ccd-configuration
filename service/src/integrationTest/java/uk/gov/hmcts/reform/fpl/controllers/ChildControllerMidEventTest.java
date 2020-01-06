package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(ChildController.class)
@OverrideAutoConfiguration(enabled = true)
class ChildControllerMidEventTest extends AbstractControllerTest {

    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    ChildControllerMidEventTest() {
        super("enter-children");
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenFutureDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", ImmutableList.of(
                createChildrenElement(LocalDate.now().plusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorWhenThereIsMultipleChildren() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", ImmutableList.of(
                    createChildrenElement(LocalDate.now().plusDays(1)),
                    createChildrenElement(LocalDate.now().plusDays(1))
                )))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsOnlyOnce(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorWhenValidDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", ImmutableList.of(
                createChildrenElement(LocalDate.now().minusDays(1)))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsWhenCaseDataIsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
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
