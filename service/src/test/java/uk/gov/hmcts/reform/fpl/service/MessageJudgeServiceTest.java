package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class MessageJudgeServiceTest {
    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final ObjectMapper mapper = new ObjectMapper();
    private final IdentityService identityService = mock(IdentityService.class);
    private final UserService userService = mock(UserService.class);
    private final MessageJudgeService messageJudgeService = new MessageJudgeService(
        time, identityService, mapper, userService
    );

    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_ABOUT = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final UUID NEW_ELEMENT_ID = UUID.randomUUID();

    @Test
    void shouldInitialiseCaseFieldsWhenC2DocumentsAndJudicialMessagesExist() {
        CaseData caseData = CaseData.builder()
            .judicialMessages(List.of(
                element(JudicialMessage.builder()
                    .latestMessage("some note")
                    .messageHistory("some history")
                    .dateSent("Some date sent")
                    .build()),
                element(JudicialMessage.builder()
                    .latestMessage("some note")
                    .messageHistory("some history")
                    .dateSent("Some date sent")
                    .build())))
            .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                .uploadedDateTime("01 Dec 2020")
                .author("Some author")
                .build())))
            .build();

        Map<String, Object> expectedData = Map.of(
            "hasC2Applications", YES.getValue(),
            "hasJudicialMessages", YES.getValue(),
            "c2DynamicList", caseData.buildC2DocumentDynamicList(),
            "judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldReturnEmptyMapWhenC2DocumentsDoNotExist() {
        assertThat(messageJudgeService.initialiseCaseFields(CaseData.builder().build())).isEmpty();
    }

    @Test
    void shouldRebuildC2DynamicListAndFormatC2DocumentsCorrectlyWhenC2HasBeenSelected() {
        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(DocumentReference.builder()
                .filename(C2_FILE_NAME)
                .build())
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
                        .build())
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .c2DynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .c2DocumentBundle(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = C2_FILE_NAME + "\n" + C2_SUPPORTING_DOCUMENT_FILE_NAME;

        assertThat(messageJudgeService.populateNewMessageFields(caseData))
            .extracting("relatedDocumentsLabel", "c2DynamicList")
            .containsExactly(expectedC2Label, caseData.buildC2DocumentDynamicList(SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    @Test
    void shouldReturnEmptyMapWhenC2DocumentHasNotBeenSelected() {
        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(DocumentReference.builder()
                .filename(C2_FILE_NAME)
                .build())
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
                        .build())
                    .build())))
            .build();

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        assertThat(messageJudgeService.populateNewMessageFields(caseData)).isEmpty();
    }

    @Test
    void shouldBuildRelatedDocumentsLabelAndRebuildJudicialMessagesDynamicListWhenReplyingToAMessage() {
        JudicialMessage selectedJudicialMessage= JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage),
                element(JudicialMessage.builder().build())))
            .build();

        JudicialMessage expectedJudicialMessage = selectedJudicialMessage.toBuilder()
            .latestMessage("")
            .recipient(MESSAGE_SENDER)
            .build();

        assertThat(messageJudgeService.populateReplyMessageFields(caseData))
            .extracting("relatedDocumentsLabel", "judicialMessageReply", "judicialMessageDynamicList")
            .containsExactly(selectedJudicialMessage.getRelatedDocumentFileNames(), expectedJudicialMessage,
                caseData.buildJudicialMessageDynamicList(SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenC2DocumentNotSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .about(MESSAGE_ABOUT)
            .recipient(MESSAGE_RECIPIENT)
            .urgency("High urgency")
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .build();

        CaseData caseData = CaseData.builder().messageJudgeEventData(messageJudgeEventData).build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);
        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        Element<JudicialMessage> expectedJudicialMessageElement = element(NEW_ELEMENT_ID, JudicialMessage.builder()
            .updatedTime(time.now())
            .status(OPEN)
            .latestMessage(MESSAGE_NOTE)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .about(MESSAGE_ABOUT)
            .urgency("High urgency")
            .messageHistory(MESSAGE_NOTE)
            .build());

        assertThat(updatedMessages).hasSize(1).first().isEqualTo(expectedJudicialMessageElement);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenC2DocumentHasBeenSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .recipient(MESSAGE_RECIPIENT)
            .build();

        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(C2_FILE_NAME)
            .build();

        DocumentReference supportingC2DocumentReference = DocumentReference.builder()
            .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
            .build();

        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2DocumentReference)
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .c2DynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .c2DocumentBundle(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        JudicialMessage newMessage = updatedMessages.get(0).getValue();
        List<Element<DocumentReference>> relatedDocuments = newMessage.getRelatedDocuments();

        assertThat(newMessage.getIsRelatedToC2()).isEqualTo(YES);
        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            selectedC2DocumentBundle.getAllC2DocumentFileNames()
        );
        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToExistingJudicialMessageList() {
        JudicialMessage newMessage = JudicialMessage.builder()
            .about(MESSAGE_ABOUT)
            .recipient(MESSAGE_RECIPIENT)
            .build();

        UUID existingJudicialMessageId = UUID.randomUUID();
        JudicialMessage existingJudicialMessage = JudicialMessage.builder().build();

        List<Element<JudicialMessage>> existingJudicialMessages = new ArrayList<>();
        existingJudicialMessages.add(element(existingJudicialMessageId, existingJudicialMessage));

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(newMessage)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(existingJudicialMessages)
            .build();

        JudicialMessage expectedNewJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .updatedTime(time.now())
            .status(OPEN)
            .about(MESSAGE_ABOUT)
            .latestMessage(MESSAGE_NOTE)
            .messageHistory(MESSAGE_NOTE)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);
        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        assertThat(updatedMessages.size()).isEqualTo(2);
        assertThat(updatedMessages).isEqualTo(List.of(
            element(existingJudicialMessageId, existingJudicialMessage),
            element(NEW_ELEMENT_ID, expectedNewJudicialMessage)
        ));
    }

    @Test
    void shouldUpdateExistingJudicialMessageWhenReplying() {
        String messageReply = "Reply to message";

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .judicialMessageReply(JudicialMessage.builder()
                .latestMessage(messageReply)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, JudicialMessage.builder()
                    .sender(MESSAGE_SENDER)
                    .recipient(MESSAGE_RECIPIENT)
                    .updatedTime(time.now().minusDays(1))
                    .status(OPEN)
                    .about(MESSAGE_ABOUT)
                    .latestMessage(MESSAGE_NOTE)
                    .messageHistory(MESSAGE_NOTE)
                    .dateSent(formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT))
                    .build())))
            .build();

        // The sender and recipient are not the wrong way round, the sender of the previous message has be made the
        // recipient of this one and the recipient has "responded" and become the sender.
        JudicialMessage expectedUpdatedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_RECIPIENT)
            .recipient(MESSAGE_SENDER)
            .about(MESSAGE_ABOUT)
            .updatedTime(time.now())
            .status(OPEN)
            .latestMessage(messageReply)
            .messageHistory(MESSAGE_NOTE + "\n" + messageReply)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT))
            .build();

       when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.replyToJudicialMessage(caseData);

        assertThat(updatedMessages.get(0)).isEqualTo(
            element(SELECTED_DYNAMIC_LIST_ITEM_ID, expectedUpdatedJudicialMessage));
    }

    @Test
    void shouldSortThreadOfJudicialMessagesByDate() {
        Element<JudicialMessage> latestJudicialMessage = buildJudicialMessageElement(time.now().plusDays(1));
        Element<JudicialMessage> pastJudicialMessage = buildJudicialMessageElement(time.now().plusMinutes(1));
        Element<JudicialMessage> oldestJudicialMessage = buildJudicialMessageElement(time.now().minusHours(1));

        List<Element<JudicialMessage>> judicialMessages = new ArrayList<>();
        judicialMessages.add(oldestJudicialMessage);
        judicialMessages.add(latestJudicialMessage);
        judicialMessages.add(pastJudicialMessage);

        List<Element<JudicialMessage>> sortedJudicialMessages
            = messageJudgeService.sortJudicialMessages(judicialMessages);

        assertThat(sortedJudicialMessages).isEqualTo(List.of(latestJudicialMessage, pastJudicialMessage,
            oldestJudicialMessage));
    }

    @Test
    void shouldPopulateFirstHearingLabelWhenHearingExists() {
        HearingType hearingType = CASE_MANAGEMENT;
        LocalDateTime hearingStartDate = LocalDateTime.now();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder()
                .type(hearingType)
                .startDate(hearingStartDate)
                .build())))
            .build();

        assertThat(messageJudgeService.getFirstHearingLabel(caseData))
            .isEqualTo(String.format("Next hearing in the case: %s hearing, %s", hearingType.getLabel(),
                formatLocalDateTimeBaseUsingFormat(hearingStartDate, DATE)));
    }

    @Test
    void shouldNotPopulateFirstHearingLabelWhenHearingDoesNotExists() {
        CaseData caseData = CaseData.builder().build();
        assertThat(messageJudgeService.getFirstHearingLabel(caseData)).isEmpty();
    }

    private Element<JudicialMessage> buildJudicialMessageElement(LocalDateTime dateTime) {
        return element(JudicialMessage.builder().updatedTime(dateTime).build());
    }
}
