package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType.CTSC;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

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
