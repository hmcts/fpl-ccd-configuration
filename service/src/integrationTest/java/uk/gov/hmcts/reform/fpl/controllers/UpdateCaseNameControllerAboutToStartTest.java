package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(UpdateCaseNameController.class)
@OverrideAutoConfiguration(enabled = true)
class UpdateCaseNameControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private CaseSubmissionService caseSubmissionService;

    UpdateCaseNameControllerAboutToStartTest() {
        super("update-case-name");
    }

    @BeforeEach
    void setup() {
        when(caseSubmissionService.generateCaseName(any())).thenReturn("LA & Bloggs, Testington");
    }

    @Test
    void shouldInitialiseCaseFieldsWhenJudicialMessagesExist() {
        final String expectedCaseName = "LA & Bloggs, Testington";
        CaseData caseData = CaseData.builder()
            .id(1111L)
            .caseName("LA & Phillips, Testington")
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData().get("caseName")).isEqualTo(expectedCaseName);
    }
}
