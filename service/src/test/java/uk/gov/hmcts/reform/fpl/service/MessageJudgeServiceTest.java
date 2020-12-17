package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

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
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final UUID NEW_ELEMENT_ID = UUID.randomUUID();

    @Test
    void shouldInitialiseCaseFieldsWhenC2DocumentsAndJudicialMessagesExist() {
        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("01 Dec 2020")
                .build()),
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("02 Dec 2020")
                .build())
        );

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(element(C2DocumentBundle.builder()
            .uploadedDateTime("01 Dec 2020")
            .author("Some author")
            .build())
        );

        CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .c2DocumentBundle(c2DocumentBundle)
            .build();

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedC2DynamicList = buildDynamicList(
            Pair.of(c2DocumentBundle.get(0).getId(), "Application 1: 01 Dec 2020")
        );

        DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020"),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasC2Applications", "Yes",
            "hasJudicialMessages", "Yes",
            "c2DynamicList", expectedC2DynamicList,
            "judicialMessageDynamicList", expectedJudicialDynamicList);

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseC2DocumentFieldsOnlyWhenJudicialMessagesDoNotExist() {
        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(element(C2DocumentBundle.builder()
            .uploadedDateTime("01 Dec 2020")
            .author("Some author")
            .build())
        );

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .build();

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedC2DynamicList = buildDynamicList(
            Pair.of(c2DocumentBundle.get(0).getId(), "Application 1: 01 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasC2Applications", "Yes",
            "c2DynamicList", expectedC2DynamicList
        );

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseJudicialFieldsOnlyWhenC2DocumentsDoNotExist() {
        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("01 Dec 2020")
                .build()),
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("02 Dec 2020")
                .build())
        );

        CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .build();

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020"),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasJudicialMessages", "Yes",
            "judicialMessageDynamicList", expectedJudicialDynamicList
        );

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

        UUID otherC2DocumentBundleId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .c2DocumentBundle(List.of(
                element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedC2DocumentBundle),
                element(otherC2DocumentBundleId, C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = C2_FILE_NAME + "\n" + C2_SUPPORTING_DOCUMENT_FILE_NAME;

        assertThat(messageJudgeService.populateNewMessageFields(caseData))
            .extracting("relatedDocumentsLabel", "c2DynamicList")
            .containsExactly(
                expectedC2Label,
                buildDynamicList(0,
                    Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "Application 1: null"),
                    Pair.of(otherC2DocumentBundleId, "Application 2: null")
                )
            );
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
        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .urgency("very")
            .build();

        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage),
            element(JudicialMessage.builder().build())
        );

        DynamicList judicialMessageDynamicList = buildDynamicList(0,
            Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "very, null"),
            Pair.of(judicialMessages.get(1).getId(), "null")
        );

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(judicialMessageDynamicList)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(judicialMessages)
            .build();

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .recipient(MESSAGE_SENDER)
            .requestedBy(selectedJudicialMessage.getRequestedBy())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage("")
            .build();

        Map<String, Object> populateReplyMessageFields = messageJudgeService.populateReplyMessageFields(caseData);

        assertThat(populateReplyMessageFields)
            .extracting("judicialMessageReply", "judicialMessageDynamicList")
            .containsExactly(
                expectedJudicialMessage,
                judicialMessageDynamicList
            );
    }

    @Test
    void shouldThrowAnExceptionWhenJudicialMessagesFailsToBeFound() {
        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "")))
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(UUID.randomUUID(), selectedJudicialMessage),
                element(JudicialMessage.builder().build())))
            .build();

        assertThatThrownBy(() -> messageJudgeService.populateReplyMessageFields(caseData))
            .isInstanceOf(JudicialMessageNotFoundException.class)
            .hasMessage(format("Judicial message with id %s not found", SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenC2DocumentNotSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .requestedBy(MESSAGE_REQUESTED_BY)
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
            .requestedBy(MESSAGE_REQUESTED_BY)
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
            .c2DynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "Application 1: null")))
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
            .requestedBy(MESSAGE_REQUESTED_BY)
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
            .requestedBy(MESSAGE_REQUESTED_BY)
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
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
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
                    .requestedBy(MESSAGE_REQUESTED_BY)
                    .latestMessage(MESSAGE_NOTE)
                    .messageHistory(MESSAGE_NOTE)
                    .dateSent(dateSent)
                    .build())))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.replyToJudicialMessage(caseData);

        // The sender and recipient are not the wrong way round, the sender of the previous message has be made the
        // recipient of this one and the recipient has "responded" and become the sender.
        Element<JudicialMessage> expectedUpdatedJudicialMessage = element(SELECTED_DYNAMIC_LIST_ITEM_ID,
            JudicialMessage.builder()
                .sender(MESSAGE_RECIPIENT)
                .recipient(MESSAGE_SENDER)
                .requestedBy(MESSAGE_REQUESTED_BY)
                .updatedTime(time.now())
                .status(OPEN)
                .latestMessage(messageReply)
                .messageHistory(MESSAGE_NOTE + "\n" + messageReply)
                .dateSent(dateSent)
                .build()
        );

        assertThat(updatedMessages.get(0)).isEqualTo(expectedUpdatedJudicialMessage);
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
