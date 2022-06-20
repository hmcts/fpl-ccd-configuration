package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherAboutToStartTest extends AbstractCallbackTest {

    RespondentControllerChangeFromOtherAboutToStartTest() {
        super("enter-respondents/change-from-other");
    }

    @Test
    void aboutToStartShouldPrePopulateOthersList() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "others", createOthers()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("othersList");
    }

    @Test
    void aboutToStartShouldReturnErrorsWhenNoOthersInCase() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).hasSize(1);
        assertThat(callbackResponse.getErrors().get(0)).isEqualTo("There is no other person in this case.");
    }
}
