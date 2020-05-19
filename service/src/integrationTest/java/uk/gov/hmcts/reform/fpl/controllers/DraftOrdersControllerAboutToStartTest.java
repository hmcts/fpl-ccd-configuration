package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Order;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(DraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class DraftOrdersControllerAboutToStartTest extends AbstractControllerTest {

    DraftOrdersControllerAboutToStartTest() {
        super("draft-standard-directions");
    }

    @Test
    void shouldPopulateDateOfIssueWithTodayWhenNoStoredDateAvailable() {
        CaseDetails caseDetails = buildCaseDetails("");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData().get("dateOfIssue")).isEqualTo(dateNow().toString());
    }

    @Test
    void shouldPopulateDateOfIssueWithStoredDateWhenStoredDateAvailable() {
        CaseDetails caseDetails = buildCaseDetails("20 March 2020");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData().get("dateOfIssue")).isEqualTo(LocalDate.of(2020, 3, 20).toString());
    }

    private CaseDetails buildCaseDetails(String date) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        return CaseDetails.builder().data(builder
            .put("standardDirectionOrder", Order.builder().dateOfIssue(date).build())
            .build())
            .build();
    }
}
