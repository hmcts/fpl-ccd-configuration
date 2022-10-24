package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class ReplyToMessageJudgeServiceTest {

    private static final String COURT_EMAIL = "ctsc@test.com";
    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final String OTHER_FILE_NAME = "other.doc";
    private static final String OTHER_SUPPORTING_DOCUMENT_FILE_NAME = "other_supporting.doc";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = randomUUID();
    private static final UUID NEW_ELEMENT_ID = randomUUID();

    @Mock
    private Time time;
    @Mock
    private UserService userService;
    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    @Spy
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private ReplyToMessageJudgeService replyToMessageJudgeService;

    @BeforeEach
    void init() {
        when(ctscEmailLookupConfiguration.getEmail()).thenReturn(COURT_EMAIL);
        when(time.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldInitialiseCaseFieldsWhenAdditionalApplicationDocumentsAndJudicialMessagesExist() {

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

        final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .uploadedDateTime("01 Dec 2020")
            .author("Some author")
            .build();

        final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .build()));

        final Element<Placement> placement = element(Placement.builder()
            .childName("Alex Green")
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 12, 13, 0))
            .build());

        final CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .placementEventData(PlacementEventData.builder()
                .placements(List.of(placement))
                .build())
            .build();

        final Map<String, Object> expectedEventData = replyToMessageJudgeService.initialiseCaseFields(caseData);

        final DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(c2DocumentBundle.getId(), "C2, 01 Dec 2020"),
            Pair.of(placement.getId(), "A50, Alex Green, 12 October 2020, 1:00pm"));

        final String expectedUrgencyText = "Lorem ipsum dolor sit amet, consectetur adipiscing "
            + "elit. Sed sollicitudin eu felis tincidunt volutpat. Donec tempus quis metus congue placerat. Sed ligula "
            + "nisl, tempor at eleifend ac, consequat condimentum sem. In sed porttitor turpis...";

        final DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020, " + expectedUrgencyText),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020, High"));

        final Map<String, Object> expectedData = Map.of(
            "hasAdditionalApplications", "Yes",
            "hasJudicialMessages", "Yes",
            "additionalApplicationsDynamicList", expectedAdditionalApplicationsDynamicList,
            "judicialMessageDynamicList", expectedJudicialDynamicList,
            "judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(COURT_EMAIL)
                .recipient(EMPTY).build());

        assertThat(expectedEventData).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseAdditionalApplicationDocumentFieldsOnlyWhenJudicialMessagesDoNotExist() {
        UUID applicationId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(applicationId)
                    .uploadedDateTime("01 Dec 2020")
                    .author("Some author")
                    .build())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        Map<String, Object> data = replyToMessageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(applicationId, "C2, 01 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasAdditionalApplications", "Yes",
            "additionalApplicationsDynamicList", expectedAdditionalApplicationsDynamicList,
            "judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(COURT_EMAIL)
                .recipient(EMPTY).build()
        );

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseJudicialFieldsOnlyWhenDocumentsDoNotExist() {
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

        Map<String, Object> data = replyToMessageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020"),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasJudicialMessages", "Yes",
            "judicialMessageDynamicList", expectedJudicialDynamicList,
            "judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(COURT_EMAIL)
                .recipient(EMPTY).build()
        );

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseJudicialMessagesWithEmailAddressesWhenDocumentsDoNotExist() {
        List<Element<JudicialMessage>> judicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("01 Dec 2020")
                .build())
        );

        List<Element<JudicialMessage>> closedJudicialMessages = List.of(
            element(JudicialMessage.builder()
                .latestMessage("some note")
                .messageHistory("some history")
                .dateSent("02 Dec 2020")
                .build())
        );

        CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .closedJudicialMessages(closedJudicialMessages)
            .build();

        Map<String, Object> data = replyToMessageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasJudicialMessages", "Yes",
            "judicialMessageDynamicList", expectedJudicialDynamicList,
            "judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(COURT_EMAIL)
                .recipient(EMPTY).build()
        );

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldPopulateOnlyEmailAddressesWhenDocumentsDoNotExist() {
        assertThat(replyToMessageJudgeService.initialiseCaseFields(CaseData.builder().build()))
            .containsExactly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .sender(COURT_EMAIL)
                    .recipient(EMPTY).build()));
    }

    @Test
    void shouldPrePopulateSenderAndRecipientEmailsWhenNewMessageIsInitiatedByJudge() {
        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);

        CaseData caseData = CaseData.builder().build();

        assertThat(replyToMessageJudgeService.initialiseCaseFields(caseData))
            .containsExactly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient(COURT_EMAIL)
                    .sender(MESSAGE_SENDER).build()));
    }

    @Test
    void shouldNotPrePopulateSenderAndRecipientEmailsWhenNewMessageIsInitiatedNotByJudge() {
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        assertThat(replyToMessageJudgeService.initialiseCaseFields(caseData))
            .containsExactly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .sender(COURT_EMAIL)
                    .recipient(EMPTY).build()));
    }

    @Test
    void shouldBuildRelatedDocumentsLabelAndRebuildJudicialMessagesDynamicListWhenReplyingToAMessage() {
        JudicialMessage selectedJudicialMessage = JudicialMessage.builder()
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
            .subject(selectedJudicialMessage.getSubject())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .urgency(selectedJudicialMessage.getUrgency())
            .replyFrom(COURT_EMAIL)
            .replyTo(MESSAGE_SENDER)
            .latestMessage("")
            .build();

        Map<String, Object> populateReplyMessageFields = replyToMessageJudgeService.populateReplyMessageFields(caseData);

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
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
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
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .recipient(MESSAGE_SENDER)
            .subject(selectedJudicialMessage.getSubject())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .urgency(selectedJudicialMessage.getUrgency())
            .replyFrom(MESSAGE_RECIPIENT)
            .replyTo(MESSAGE_SENDER)
            .latestMessage("")
            .build();

        Map<String, Object> populateReplyMessageFields = replyToMessageJudgeService.populateReplyMessageFields(caseData);

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
    void shouldReturnValidationErrorWhenFromAndToEmailAddressesAreSame() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YES.getValue())
                .latestMessage("reply message")
                .replyFrom(MESSAGE_SENDER)
                .replyTo(MESSAGE_SENDER)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        assertThat(replyToMessageJudgeService.validateJudgeReplyMessage(caseData))
            .containsExactly("The sender's and recipient's email address cannot be the same");
    }

    @Test
    void shouldNotReturnValidationErrorWhenJudgeReplyHaveDifferentSenderAndRecipientEmailAddresses() {
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YES.getValue())
                .replyFrom(MESSAGE_SENDER)
                .replyTo(MESSAGE_RECIPIENT)
                .latestMessage("reply")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        assertThat(replyToMessageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotValidateWhenSenderEmailAddressesIsEmpty(String senderEmail) {
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(YES.getValue())
                .replyFrom(senderEmail)
                .replyTo(MESSAGE_RECIPIENT)
                .latestMessage("reply")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        assertThat(replyToMessageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"No"})
    void shouldNotValidateWhenJudgeIsNotReplyingToMessage(String replyingToMessage) {
        String dateSent = formatLocalDateTimeBaseUsingFormat(time.now().minusDays(1), DATE_TIME_AT);

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageDynamicList(buildDynamicList(0, Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, dateSent)))
            .judicialMessageReply(JudicialMessage.builder()
                .isReplying(replyingToMessage)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(List.of(element(SELECTED_DYNAMIC_LIST_ITEM_ID, buildJudicialMessage(dateSent))))
            .build();

        assertThat(replyToMessageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
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

        String formattedMessageHistory = String.format("%s - %s", MESSAGE_SENDER, MESSAGE_NOTE) + "\n \n"
            + String.format("%s - %s", MESSAGE_RECIPIENT, messageReply);

        // The sender and recipient are not the wrong way round, the sender of the previous message has be made the
        // recipient of this one and the recipient has "responded" and become the sender.
        Element<JudicialMessage> expectedUpdatedJudicialMessage = element(SELECTED_DYNAMIC_LIST_ITEM_ID,
            JudicialMessage.builder()
                .sender(MESSAGE_RECIPIENT)
                .recipient(MESSAGE_SENDER)
                .subject(MESSAGE_REQUESTED_BY)
                .updatedTime(time.now())
                .status(OPEN)
                .latestMessage(messageReply)
                .messageHistory(formattedMessageHistory)
                .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
                .build()
        );

        assertThat(updatedMessages.get(0)).isEqualTo(expectedUpdatedJudicialMessage);
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
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_RECIPIENT);
        List<Element<JudicialMessage>> expectedJudicialMessages = List.of(oldJudicialMessage);

        Map<String, Object> updatedJudicialMessages = replyToMessageJudgeService.updateJudicialMessages(caseData);

        assertThat(updatedJudicialMessages).containsEntry("judicialMessages", expectedJudicialMessages);

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
                .replyFrom(sender)
                .replyTo(recipient)
                .build())
            .build();
    }

    private Element<JudicialMessage> buildJudicialMessageElement(LocalDateTime dateTime, JudicialMessageStatus status) {
        return element(JudicialMessage.builder().updatedTime(dateTime).status(status).build());
    }

    private JudicialMessage buildJudicialMessage(String dateSent) {
        return JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .updatedTime(time.now().minusDays(1))
            .status(OPEN)
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(MESSAGE_NOTE)
            .messageHistory(String.format("%s - %s", MESSAGE_SENDER, MESSAGE_NOTE))
            .dateSent(dateSent)
            .build();
    }

}
