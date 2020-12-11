package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToSubmitTest extends AbstractControllerTest {
    MessageJudgeControllerAboutToSubmitTest() {
        super("message-judge");
    }

    @MockBean
    private IdentityService identityService;

    @Test
    void shouldAddNewJudicialMessageAndSortIntoExisting() {
        UUID judicialMessageId = UUID.randomUUID();

        JudicialMessage oldJudicialMessage = JudicialMessage.builder()
            .dateSentAsLocalDateTime(now().minusDays(1))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .relatedDocumentsLabel("related documents")
            .judicialMessageNote("Some note")
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .urgency("High urgency")
                .sender("ben@fpla.com")
                .recipient("John@fpla.com")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .judicialMessages(List.of(element(oldJudicialMessage)))
            .messageJudgeEventData(messageJudgeEventData)
            .build();

        when(identityService.generateId()).thenReturn(judicialMessageId);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));
        CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .dateSent(formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME_AT))
            .dateSentAsLocalDateTime(now())
            .status(OPEN)
            .recipient("John@fpla.com")
            .note("Some note")
            .sender("ben@fpla.com")
            .urgency("High urgency")
            .build();

        assertThat(responseCaseData.getJudicialMessages().get(0).getValue()).isEqualTo(expectedJudicialMessage);
        assertThat(responseCaseData.getJudicialMessages().get(1).getValue()).isEqualTo(oldJudicialMessage);
    }

    @Test
    void shouldRemoveTransientFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "hasC2Applications", "some data",
                "isMessageRegardingC2", "some data",
                "c2DynamicList", "some data",
                "relatedDocumentsLabel", "some data",
                "nextHearingLabel", "some data",
                "judicialMessageMetaData", JudicialMessageMetaData.builder()
                        .recipient("some data")
                        .sender("some data")
                        .urgency("some data")
                    .build(),
                "judicialMessageNote", "some data"
                ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData().get("hasC2Applications")).isNull();
        assertThat(response.getData().get("isMessageRegardingC2")).isNull();
        assertThat(response.getData().get("c2DynamicList")).isNull();
        assertThat(response.getData().get("relatedDocumentsLabel")).isNull();
        assertThat(response.getData().get("nextHearingLabel")).isNull();
        assertThat(response.getData().get("judicialMessageMetaData")).isNull();
        assertThat(response.getData().get("judicialMessageNote")).isNull();
    }
}
