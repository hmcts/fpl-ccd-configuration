package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final JudicialMessageRoleType SENDER_TYPE = JudicialMessageRoleType.LOCAL_COURT_ADMIN;
    private static final JudicialMessageRoleType RECIPIENT_TYPE = JudicialMessageRoleType.OTHER;
    private static final String SENDER = "ben@fpla.com";
    private static final String MESSAGE = "Some message";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();

    MessageJudgeControllerAboutToSubmitTest() {
        super("message-judge");
    }

    @MockBean
    private IdentityService identityService;

    @MockBean
    private UserService userService;

    @Test
    void shouldAddNewJudicialMessageAndSortIntoExistingJudicialMessageList() {
        JudicialMessage oldJudicialMessage = JudicialMessage.builder()
            .updatedTime(now().minusDays(1))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .relatedDocumentsLabel("related documents")
            .judicialMessageNote(MESSAGE)
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .urgency("High urgency")
                .recipient(MESSAGE_RECIPIENT)
                .recipientType(RECIPIENT_TYPE)
                .sender(SENDER)
                .senderType(SENDER_TYPE)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .judicialMessages(List.of(element(oldJudicialMessage)))
            .messageJudgeEventData(messageJudgeEventData)
            .build();

        when(identityService.generateId()).thenReturn(SELECTED_DYNAMIC_LIST_ITEM_ID);
        when(userService.getUserEmail()).thenReturn(SENDER);

        CaseData responseCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .dateSent(formatLocalDateTimeBaseUsingFormat(now(), DATE_TIME_AT))
            .updatedTime(now())
            .status(OPEN)
            .recipient(MESSAGE_RECIPIENT)
            .recipientType(RECIPIENT_TYPE)
            .latestMessage(MESSAGE)
            .sender(SENDER)
            .senderType(SENDER_TYPE)
            .messageHistory(String.format("%s - %s", SENDER, MESSAGE))
            .urgency("High urgency")
            .build();

        assertThat(responseCaseData.getJudicialMessages().get(0).getValue()).isEqualTo(expectedJudicialMessage);
        assertThat(responseCaseData.getJudicialMessages().get(1).getValue()).isEqualTo(oldJudicialMessage);
        assertThat(responseCaseData.getLatestRoleSent()).isEqualTo(RECIPIENT_TYPE);

    }

    @Test
    void shouldRemoveTransientFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.ofEntries(
                Map.entry("documentDynamicList",
                    buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data"))),
                Map.entry("documentTypesDynamicList",
                    buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data"))),
                Map.entry("hasAdditionalApplications", "some data"),
                Map.entry("isMessageRegardingDocuments", "APPLICATION"),
                Map.entry("judicialMessageDynamicList",
                    buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "some data"))),
                Map.entry("relatedDocumentsLabel", "some data"),
                Map.entry("nextHearingLabel", "some data"),
                Map.entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient("some data")
                    .sender("some data")
                    .urgency("some data")
                    .build()),
                Map.entry("judicialMessageNote", "some data"),
                Map.entry("judicialMessageReply", JudicialMessage.builder().build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "documentDynamicList",
            "documentTypesDynamicList",
            "hasAdditionalApplications",
            "isMessageRegardingDocuments",
            "judicialMessageDynamicList",
            "additionalApplicationsDynamicList",
            "relatedDocumentsLabel",
            "nextHearingLabel",
            "judicialMessageMetaData",
            "judicialMessageNote",
            "judicialMessageReply"
        );
    }
}
