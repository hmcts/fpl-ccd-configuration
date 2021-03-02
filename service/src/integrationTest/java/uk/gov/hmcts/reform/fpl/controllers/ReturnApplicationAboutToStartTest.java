package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;

@ActiveProfiles("integration-test")
@WebMvcTest(ReturnApplicationController.class)
@OverrideAutoConfiguration(enabled = true)
class ReturnApplicationAboutToStartTest extends AbstractControllerTest {

    private static final String RETURN_APPLICATION = "returnApplication";

    ReturnApplicationAboutToStartTest() {
        super("return-application");
    }

    @Test
    void shouldResetReturnApplicationProperties() {
        CaseData caseData = CaseData.builder()
            .returnApplication(ReturnApplication.builder()
                .reason(List.of(INCOMPLETE))
                .note("Some old reason")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).doesNotContainKey(RETURN_APPLICATION);
    }
}
