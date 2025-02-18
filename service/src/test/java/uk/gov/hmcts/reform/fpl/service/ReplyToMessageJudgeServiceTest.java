package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ReplyToMessageJudgeServiceTest {

    private static final String COURT_EMAIL = "ctsc@test.com";
    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = randomUUID();

    @Mock
    private Time time;
    @Mock
    private UserService userService;
    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private JudicialService judicialService;
    @Spy
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private ReplyToMessageJudgeService replyToMessageJudgeService;

    @BeforeEach
    void init() {
        when(ctscEmailLookupConfiguration.getEmail()).thenReturn(COURT_EMAIL);
        when(roleAssignmentService.getJudicialCaseRolesAtTime(any(), any())).thenReturn(List.of());

        when(time.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldInitialiseCaseFieldsWhenJudicialMessagesExist() {

        final String longUrgency = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sollicitudin eu felis "
            + "tincidunt volutpat. Donec tempus quis metus congue placerat. Sed ligula nisl, tempor at eleifend ac, "
            + "consequat condimentum sem. In sed porttitor turpis, at laoreet quam. Fusce bibendum vehicula ipsum, et "
            + "tempus ante fermentum non.";

        final List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .urgency(longUrgency)
                .dateSent("01 Dec 2020")
                .build()),
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("02 Dec 2020")
                .urgency("High")
                .build()));

        final CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .build();

        final Map<String, Object> expectedEventData = replyToMessageJudgeService.initialiseCaseFields(caseData);


        final String expectedUrgencyText = "Lorem ipsum dolor sit amet, consectetur adipiscing "
            + "elit. Sed sollicitudin eu felis tincidunt volutpat. Donec tempus quis metus congue placerat. Sed ligula "
            + "nisl, tempor at eleifend ac, consequat condimentum sem. In sed porttitor turpis...";

        final DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020, " + expectedUrgencyText),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020, High"));

        final Map<String, Object> expectedData = Map.of(
            "hasJudicialMessages", YES,
            "judicialMessageDynamicList", expectedJudicialDynamicList);

        assertThat(expectedEventData).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseCaseFieldsWhenNoJudicialMessage() {
        final CaseData caseData = CaseData.builder().build();

        final Map<String, Object> expectedEventData = replyToMessageJudgeService.initialiseCaseFields(caseData);

        final DynamicList expectedJudicialDynamicList = buildDynamicList();

        final Map<String, Object> expectedData = Map.of(
            "hasJudicialMessages", NO,
            "judicialMessageDynamicList", expectedJudicialDynamicList);

        assertThat(expectedEventData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildRelatedDocumentsLabelAndRebuildJudicialMessagesDynamicListWhenReplyingToAMessage() {
        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .senderType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .subject("Test subject")
            .dateSent("12 January 2021")
            .urgency("high")
            .build();

        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage),
            element(JudicialMessage.builder().build())
        );

        DynamicList judicialMessageDynamicList = buildDynamicList(0,
            Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "Test subject, 12 January 2021, high"),
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
            .senderType(JudicialMessageRoleType.OTHER)
            .recipientDynamicList(buildRecipientDynamicList(JudicialMessageRoleType.LOCAL_COURT_ADMIN))
            .recipientLabel("Local Court Admin (%s)".formatted(MESSAGE_SENDER))
            .subject(selectedJudicialMessage.getSubject())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .urgency(selectedJudicialMessage.getUrgency())
            .replyFrom(MESSAGE_RECIPIENT)
            .replyTo(MESSAGE_SENDER)
            .latestMessage("")
            .build();

        Map<String, Object> populateReplyMessageFields =
            replyToMessageJudgeService.populateReplyMessageFields(caseData);

        assertThat(populateReplyMessageFields)
            .extracting("judicialMessageReply", "judicialMessageDynamicList")
            .containsExactly(
                expectedJudicialMessage,
                judicialMessageDynamicList
            );
    }

    @Test
    void shouldPopulateReplyFromWithJudgeEmailAddressWhenJudgeIsReplyingToAMessage() {
        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
            .senderType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
            .sender(MESSAGE_SENDER)
            .relatedDocumentFileNames("file1.doc")
            .messageHistory("message history")
            .latestMessage("Some note")
            .subject("Test subject")
            .dateSent("12 January 2021")
            .urgency("high")
            .build();

        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(SELECTED_DYNAMIC_LIST_ITEM_ID, selectedJudicialMessage),
            element(JudicialMessage.builder().build())
        );

        DynamicList judicialMessageDynamicList = buildDynamicList(0,
            Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "Test subject, 12 January 2021, high"),
            Pair.of(judicialMessages.get(1).getId(), "null")
        );

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(judicialMessageDynamicList)

            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(judicialMessages)
            .build();

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .senderType(JudicialMessageRoleType.OTHER)
            .recipientDynamicList(buildRecipientDynamicList(JudicialMessageRoleType.LOCAL_COURT_ADMIN))
            .recipientLabel("Local Court Admin (%s)".formatted(MESSAGE_SENDER))
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .subject(selectedJudicialMessage.getSubject())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .urgency(selectedJudicialMessage.getUrgency())
            .replyFrom(MESSAGE_RECIPIENT)
            .replyTo(MESSAGE_SENDER)
            .latestMessage("")
            .build();

        Map<String, Object> populateReplyMessageFields =
            replyToMessageJudgeService.populateReplyMessageFields(caseData);

        assertThat(populateReplyMessageFields)
            .extracting("judicialMessageReply")
            .isEqualTo(expectedJudicialMessage);
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
                element(randomUUID(), selectedJudicialMessage),
                element(JudicialMessage.builder().build())))
            .build();

        assertThatThrownBy(() -> replyToMessageJudgeService.populateReplyMessageFields(caseData))
            .isInstanceOf(JudicialMessageNotFoundException.class)
            .hasMessage(format("Judicial message with id %s not found", SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateExistingJudicialMessageWhenReplying() {
        String messageReply = "Reply to message";
        String originalSentDate = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData
            = buildMessageEventData(messageReply, originalSentDate, true);

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(originalSentDate))))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);

        Map<String, Object> updatedData = replyToMessageJudgeService.updateJudicialMessages(caseData);
        List<Element<JudicialMessage>> updatedMessages =
            (List<Element<JudicialMessage>>) updatedData.get("judicialMessages");

        String formattedMessageHistory = String.format("%s (%s) - %s",
            JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel(), MESSAGE_SENDER, MESSAGE_NOTE)
            + "\n \n"
            + String.format("%s (%s) - %s", JudicialMessageRoleType.OTHER.getLabel(), MESSAGE_RECIPIENT, messageReply);

        // The sender and recipient are not the wrong way round, the sender of the previous message has be made the
        // recipient of this one and the recipient has "responded" and become the sender.
        Element<JudicialMessage> expectedUpdatedJudicialMessage = element(SELECTED_DYNAMIC_LIST_ITEM_ID,
            JudicialMessage.builder()
                .sender(MESSAGE_RECIPIENT)
                .recipientType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
                .senderType(JudicialMessageRoleType.OTHER)
                .subject(MESSAGE_REQUESTED_BY)
                .updatedTime(time.now())
                .status(OPEN)
                .latestMessage(messageReply)
                .messageHistory(formattedMessageHistory)
                .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
                .build()
        );

        assertThat(updatedMessages.get(0)).isEqualTo(expectedUpdatedJudicialMessage);
        assertThat(updatedData.get("latestRoleSent")).isEqualTo(JudicialMessageRoleType.LOCAL_COURT_ADMIN);
    }

    @Test
    void shouldSortThreadOfJudicialMessagesByDate() {
        Element<JudicialMessage> latestJudicialMessage = buildJudicialMessageElement(time.now().plusDays(1), OPEN);
        Element<JudicialMessage> pastJudicialMessage = buildJudicialMessageElement(time.now().plusMinutes(1), OPEN);
        Element<JudicialMessage> oldestJudicialMessage = buildJudicialMessageElement(time.now().minusHours(1), OPEN);

        List<Element<JudicialMessage>> judicialMessages = new ArrayList<>();
        judicialMessages.add(oldestJudicialMessage);
        judicialMessages.add(latestJudicialMessage);
        judicialMessages.add(pastJudicialMessage);

        List<Element<JudicialMessage>> sortedJudicialMessages
            = replyToMessageJudgeService.sortJudicialMessages(judicialMessages);

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

        assertThat(replyToMessageJudgeService.getNextHearingLabel(caseData))
            .isEqualTo(String.format("Next hearing in the case: %s hearing, %s", hearingType.getLabel(),
                formatLocalDateTimeBaseUsingFormat(hearingStartDate, DATE)));
    }

    @Test
    void shouldNotPopulateFirstHearingLabelWhenHearingDoesNotExists() {
        CaseData caseData = CaseData.builder().build();

        assertThat(replyToMessageJudgeService.getNextHearingLabel(caseData)).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCloseJudicialMessageWhenIsReplyingToJudicialMessageIsSelectedAsNo() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = buildMessageEventData(null, dateSent, false);

        Element<JudicialMessage> oldJudicialMessage = buildJudicialMessageElement(time.now().minusDays(1), OPEN);

        Element<JudicialMessage> selectedJudicialMessage = element(
            SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent));
        Element<JudicialMessage> closedJudicialMessage = buildJudicialMessageElement(time.now().minusDays(2), CLOSED);

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(oldJudicialMessage, selectedJudicialMessage))
            .closedJudicialMessages(List.of(closedJudicialMessage))
            .latestRoleSent(JudicialMessageRoleType.OTHER)
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);
        List<Element<JudicialMessage>> expectedJudicialMessages = List.of(oldJudicialMessage);

        Map<String, Object> updatedJudicialMessages = replyToMessageJudgeService.updateJudicialMessages(caseData);

        assertThat(updatedJudicialMessages).containsEntry("judicialMessages", expectedJudicialMessages);
        assertThat(updatedJudicialMessages).doesNotContainKeys("latestRoleSent");

        List<Element<JudicialMessage>> updatedClosedMessages =
            (List<Element<JudicialMessage>>) updatedJudicialMessages.get("closedJudicialMessages");
        assertThat(updatedClosedMessages)
            .extracting(Element::getId, judicialMessageElement -> judicialMessageElement.getValue().getStatus())
            .containsExactly(
                tuple(selectedJudicialMessage.getId(), CLOSED),
                tuple(closedJudicialMessage.getId(), CLOSED));
    }

    @Test
    void shouldThrowAnExceptionWhenSelectedJudicialMessageToCloseIsNotFound() {
        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "")))
            .judicialMessageReply(JudicialMessage.builder().isReplying(NO.getValue()).build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(
                element(randomUUID(), JudicialMessage.builder().build()),
                element(randomUUID(), JudicialMessage.builder().build())))
            .build();

        assertThatThrownBy(() -> replyToMessageJudgeService.updateJudicialMessages(caseData))
            .isInstanceOf(JudicialMessageNotFoundException.class)
            .hasMessage(format("Judicial message with id %s not found", SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    private MessageJudgeEventData buildMessageEventData(String messageReply, String dateSent, boolean isReplying) {
        if (isReplying) {
            return buildMessageEventData(messageReply, dateSent, true, MESSAGE_RECIPIENT, MESSAGE_SENDER);
        } else {
            return buildMessageEventData(messageReply, dateSent, false, MESSAGE_SENDER, MESSAGE_RECIPIENT);
        }
    }

    private MessageJudgeEventData buildMessageEventData(
        String messageReply, String dateSent, boolean isReplying, String sender, String recipient) {

        return MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(isReplying ? YES.getValue() : NO.getValue())
                .latestMessage(isReplying ? messageReply : null)
                .recipientDynamicList(buildRecipientDynamicList(JudicialMessageRoleType.LOCAL_COURT_ADMIN))
                .replyFrom(sender)
                .replyTo(recipient)
                .recipientType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
                .senderType(JudicialMessageRoleType.OTHER)
                .build())
            .build();
    }

    private Element<JudicialMessage> buildJudicialMessageElement(LocalDateTime dateTime, JudicialMessageStatus status) {
        return element(JudicialMessage.builder().updatedTime(dateTime).status(status).build());
    }

    private JudicialMessage buildJudicialMessage(String dateSent) {
        return JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .senderType(JudicialMessageRoleType.LOCAL_COURT_ADMIN)
            .recipientType(JudicialMessageRoleType.OTHER)
            .updatedTime(time.now().minusDays(1))
            .status(OPEN)
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(MESSAGE_NOTE)
            .messageHistory(String.format("%s (%s) - %s",
                JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel(), MESSAGE_SENDER, MESSAGE_NOTE))
            .dateSent(dateSent)
            .build();
    }

    private DynamicList buildRecipientDynamicList(JudicialMessageRoleType selected) {
        DynamicList.DynamicListBuilder builder = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder()
                    .code(JudicialMessageRoleType.CTSC.toString())
                    .label(JudicialMessageRoleType.CTSC.getLabel())
                    .build(),
                DynamicListElement.builder()
                    .code(JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString())
                    .label(JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel())
                    .build()
            ));

        if (selected != null) {
            builder.value(DynamicListElement.builder()
                .code(selected.toString())
                .label(selected.getLabel())
                .build());
        }

        return builder.build();
    }

}
