package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.rd.client.JudicialApi;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class AllocatedJudgeControllerAboutToStartTest extends AbstractCallbackTest {

    AllocatedJudgeControllerAboutToStartTest() {
        super("allocated-judge");
    }

    @Test
    void shouldSetEnterManuallyDefaultToNo() {
        CaseData caseData = CaseData.builder()
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData after = extractCaseData(callbackResponse);

        assertThat(after.getEnterManually()).isEqualTo(YesNo.NO);
    }

}
