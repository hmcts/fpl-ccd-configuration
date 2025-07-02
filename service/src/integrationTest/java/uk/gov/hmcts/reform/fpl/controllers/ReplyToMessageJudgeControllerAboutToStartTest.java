package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType.CTSC;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(ReplyToMessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class ReplyToMessageJudgeControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    ReplyToMessageJudgeControllerAboutToStartTest() {
        super("reply-message-judge");
    }

    @BeforeEach
    void beforeEach() {
        when(featureToggleService.isCourtNotificationEnabledForWa(any())).thenReturn(true);
    }

    @Test
    void shouldInitialiseCaseFieldsWhenJudicialMessagesExist() {
        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("Some date sent")
                .build()),
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("Some date sent")
                .build())
        );

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .judicialMessages(judicialMessages)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList judicialMessageDynamicList = mapper.convertValue(
            response.getData().get("judicialMessageDynamicList"), DynamicList.class
        );

        DynamicList expectedJudicialMessageDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "Some date sent"),
            Pair.of(judicialMessages.get(1).getId(), "Some date sent")
        );

        assertThat(judicialMessageDynamicList).isEqualTo(expectedJudicialMessageDynamicList);
    }

    @Test
    void shouldClearLatestRoleSent() {
        CaseData caseData = CaseData.builder()
            .latestRoleSent(CTSC)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);
        assertThat(response.getData().get("latestRoleSent")).isNull();
    }
}
