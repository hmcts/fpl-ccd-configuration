package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.RETURN_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationAboutToStartTest extends AbstractControllerTest {
    ReturnApplicationAboutToStartTest() {
        super("return-application");
    }

    @Test
    void shouldResetReturnApplicationProperties() {
        Map<String, Object> data = ImmutableMap.of(
            RETURN_APPLICATION, ReturnApplication.builder()
                .reason(List.of(INCOMPLETE))
                .note("Some reason")
                .build());

        CaseDetails caseDetails = buildCaseDetails(data);
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);
        assertThat(response.getData().get(RETURN_APPLICATION)).isNull();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> data) {
        return CaseDetails.builder()
            .id(1111L)
            .state(OPEN.getValue())
            .data(data)
            .build();
    }
}
