package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToStartTest extends AbstractControllerTest {
    MessageJudgeControllerAboutToStartTest() {
        super("message-judge");
    }

    @Test
    void shouldBuildC2DynamicListIfCaseDataHasC2Documents() {
        CaseData caseData = CaseData.builder()
            .id(1111L)
            .c2DocumentBundle(List.of(
                element(C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("c2.doc")
                        .build())
                    .build()),
                element(C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("c2_additional.doc")
                        .build())
                    .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));
        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("c2DynamicList"), DynamicList.class
        );

        assertThat(builtDynamicList).isEqualTo(caseData.buildC2DocumentDynamicList());
        assertThat(response.getData().get("hasC2Applications")).isEqualTo(YES.getValue());
    }

    @Test
    void shouldNotBuildC2DynamicListIfCaseDataHasC2Documents() {
        CaseData caseData = CaseData.builder().id(1111L).build();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("c2DynamicList")).isNull();
        assertThat(response.getData().get("hasC2Applications")).isNull();
    }
}
