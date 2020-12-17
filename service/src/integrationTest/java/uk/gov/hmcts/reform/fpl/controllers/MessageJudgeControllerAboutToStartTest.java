package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToStartTest extends AbstractControllerTest {
    MessageJudgeControllerAboutToStartTest() {
        super("message-judge");
    }

    @Test
    void shouldInitialiseCaseFieldsWhenC2DocumentsAndJudicialMessagesExist() {

        List<Element<C2DocumentBundle>> c2Bundles = List.of(
            element(C2DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("c2.doc")
                    .build())
                .build()),
            element(C2DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("c2_additional.doc")
                    .build())
                .build())
        );

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
            .c2DocumentBundle(c2Bundles)
            .judicialMessages(judicialMessages)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        DynamicList c2DynamicList = mapper.convertValue(response.getData().get("c2DynamicList"), DynamicList.class);

        DynamicList judicialMessageDynamicList = mapper.convertValue(
            response.getData().get("judicialMessageDynamicList"), DynamicList.class
        );

        DynamicList expectedC2DynamicList = buildDynamicList(
            Pair.of(c2Bundles.get(0).getId(), "Application 1: null"),
            Pair.of(c2Bundles.get(1).getId(), "Application 2: null")
        );

        DynamicList expectedJudicialMessageDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "Some date sent"),
            Pair.of(judicialMessages.get(1).getId(), "Some date sent")
        );

        assertThat(c2DynamicList).isEqualTo(expectedC2DynamicList);
        assertThat(judicialMessageDynamicList).isEqualTo(expectedJudicialMessageDynamicList);

        assertThat(response.getData().get("hasC2Applications")).isEqualTo(YES.getValue());
        assertThat(response.getData().get("hasJudicialMessages")).isEqualTo(YES.getValue());
    }

    @Test
    void shouldNotInitialiseCaseFieldsWhenJudicialMessagesAndC2DocumentsDoNotExist() {
        CaseData caseData = CaseData.builder().id(1111L).build();
        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("c2DynamicList")).isNull();
        assertThat(response.getData().get("judicialMessageDynamicList")).isNull();
        assertThat(response.getData().get("hasC2Applications")).isNull();
        assertThat(response.getData().get("hasJudicialMessages")).isNull();
    }
}
