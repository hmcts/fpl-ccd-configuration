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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
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

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageJudgeServiceTest {

    private static final String COURT_EMAIL = "ctsc@test.com";
    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final String OTHER_FILE_NAME = "other.doc";
    private static final String OTHER_SUPPORTING_DOCUMENT_FILE_NAME = "other_supporting.doc";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final UUID NEW_ELEMENT_ID = UUID.randomUUID();

    @Mock
    private Time time;
    @Mock
    private IdentityService identityService;
    @Mock
    private UserService userService;
    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    @Spy
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private MessageJudgeService messageJudgeService;

    @BeforeEach
    void init() {
        when(ctscEmailLookupConfiguration.getEmail()).thenReturn(COURT_EMAIL);
        when(time.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldInitialiseCaseFieldsWhenAdditionalApplicationDocumentsAndJudicialMessagesExist() {
        UUID uuid = randomUUID();

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

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(uuid)
                    .uploadedDateTime("01 Dec 2020")
                    .author("Some author")
                    .build())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .judicialMessages(judicialMessages)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(uuid, "C2, 01 Dec 2020")
        );

        DynamicList expectedJudicialDynamicList = buildDynamicList(
            Pair.of(judicialMessages.get(0).getId(), "01 Dec 2020"),
            Pair.of(judicialMessages.get(1).getId(), "02 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "hasAdditionalApplications", "Yes",
            "hasJudicialMessages", "Yes",
            "additionalApplicationsDynamicList", expectedAdditionalApplicationsDynamicList,
            "judicialMessageDynamicList", expectedJudicialDynamicList,
            "judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(COURT_EMAIL)
                .recipient(EMPTY).build());

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldInitialiseAdditionalApplicationDocumentFieldsOnlyWhenJudicialMessagesDoNotExist() {
        UUID uuid = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(uuid)
                    .uploadedDateTime("01 Dec 2020")
                    .author("Some author")
                    .build())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

        DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(uuid, "C2, 01 Dec 2020")
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

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

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

        Map<String, Object> data = messageJudgeService.initialiseCaseFields(caseData);

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
        assertThat(messageJudgeService.initialiseCaseFields(CaseData.builder().build()))
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

        assertThat(messageJudgeService.initialiseCaseFields(caseData))
            .containsExactly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient(COURT_EMAIL)
                    .sender(MESSAGE_SENDER).build()));
    }

    @Test
    void shouldNotPrePopulateSenderAndRecipientEmailsWhenNewMessageIsInitiatedNotByJudge() {
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        assertThat(messageJudgeService.initialiseCaseFields(caseData))
            .containsExactly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .sender(COURT_EMAIL)
                    .recipient(EMPTY).build()));
    }

    @Test
    void shouldRebuildAdditionalApplicationDynamicListAndFormatDocumentsCorrectlyWhenOtherApplicationSelected() {
        UUID c2DocumentBundleId = UUID.randomUUID();

        OtherApplicationsBundle selectedOtherDocumentBundle = OtherApplicationsBundle.builder()
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .uploadedDateTime("1 January 2021, 12:00pm")
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .document(DocumentReference.builder()
                .filename(OTHER_FILE_NAME)
                .build())
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(OTHER_SUPPORTING_DOCUMENT_FILE_NAME)
                        .build())
                    .build())))
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(c2DocumentBundleId)
            .uploadedDateTime("2 January 2021, 12:00pm")
            .document(DocumentReference.builder()
                .filename(C2_FILE_NAME)
                .build())
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(selectedOtherDocumentBundle)
            .c2DocumentBundle(c2DocumentBundle)
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();

        String expectedRelatedDocumentsLabel = OTHER_FILE_NAME + "\n" + OTHER_SUPPORTING_DOCUMENT_FILE_NAME;

        assertThat(messageJudgeService.populateNewMessageFields(caseData))
            .extracting("relatedDocumentsLabel", "additionalApplicationsDynamicList")
            .containsExactly(
                expectedRelatedDocumentsLabel,
                buildDynamicList(0,
                    Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "C1, 1 January 2021, 12:00pm"),
                    Pair.of(c2DocumentBundleId, "C2, 2 January 2021, 12:00pm")
                )
            );
    }

    @Test
    void shouldRebuildAdditionalApplicationDynamicListAndFormatDocumentsCorrectlyWhenC2ApplicationSelected() {
        UUID otherDocumentBundleId = UUID.randomUUID();

        OtherApplicationsBundle selectedOtherDocumentBundle = OtherApplicationsBundle.builder()
            .id(otherDocumentBundleId)
            .uploadedDateTime("1 January 2021, 12:00pm")
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .document(DocumentReference.builder()
                .filename(OTHER_FILE_NAME)
                .build())
            .build();

        C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .uploadedDateTime("2 January 2021, 12:00pm")
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

        AdditionalApplicationsBundle additionalApplicationsBundle = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(selectedOtherDocumentBundle)
            .c2DocumentBundle(c2DocumentBundle)
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
            .build();

        String expectedRelatedDocumentsLabel = C2_FILE_NAME + "\n" + C2_SUPPORTING_DOCUMENT_FILE_NAME;

        assertThat(messageJudgeService.populateNewMessageFields(caseData))
            .extracting("relatedDocumentsLabel", "additionalApplicationsDynamicList")
            .containsExactly(
                expectedRelatedDocumentsLabel,
                buildDynamicList(1,
                    Pair.of(otherDocumentBundleId, "C1, 1 January 2021, 12:00pm"),
                    Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "C2, 2 January 2021, 12:00pm")
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
    void shouldReturnEmptyMapWhenAdditionalApplicationDocumentHasNotBeenSelected() {
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

        C2DocumentBundle notSelectedC2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .document(DocumentReference.builder()
                .filename("other_c2.doc")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(selectedC2DocumentBundle)
                        .build(),
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(notSelectedC2DocumentBundle)
                        .build()
                )
            )
            .build();

        assertThat(messageJudgeService.populateNewMessageFields(caseData)).isEmpty();
    }

    @Test
    void shouldNotPrePopulateRecipientWhenMessageIsInitiatedNotByJudge() {
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        assertThat(messageJudgeService.populateNewMessageFields(caseData)).isEmpty();
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
            Pair.of(SELECTED_DYNAMIC_LIST_ITEM_ID, "Test subject, 12 January 2021, Urgent"),
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

        Map<String, Object> populateReplyMessageFields = messageJudgeService.populateReplyMessageFields(caseData);

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

        Map<String, Object> populateReplyMessageFields = messageJudgeService.populateReplyMessageFields(caseData);

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
                element(UUID.randomUUID(), selectedJudicialMessage),
                element(JudicialMessage.builder().build())))
            .build();

        assertThatThrownBy(() -> messageJudgeService.populateReplyMessageFields(caseData))
            .isInstanceOf(JudicialMessageNotFoundException.class)
            .hasMessage(format("Judicial message with id %s not found", SELECTED_DYNAMIC_LIST_ITEM_ID));
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenDocumentNotSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .subject(MESSAGE_REQUESTED_BY)
            .sender(MESSAGE_SENDER)
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
            .subject(MESSAGE_REQUESTED_BY)
            .urgency("High urgency")
            .messageHistory(String.format("%s - %s", MESSAGE_SENDER, MESSAGE_NOTE))
            .build());

        assertThat(updatedMessages).hasSize(1).first().isEqualTo(expectedJudicialMessageElement);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenAdditionalApplicationDocumentHasBeenSelected() {
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
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2DocumentReference)
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2DocumentBundle)
                .build()))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        JudicialMessage newMessage = updatedMessages.get(0).getValue();
        List<Element<DocumentReference>> relatedDocuments = newMessage.getRelatedDocuments();

        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            selectedC2DocumentBundle.getAllC2DocumentFileNames()
        );
        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenOtherApplicationDocumentHasBeenSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .recipient(MESSAGE_RECIPIENT)
            .build();

        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(C2_FILE_NAME)
            .build();

        DocumentReference supportingC2DocumentReference = DocumentReference.builder()
            .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
            .build();

        OtherApplicationsBundle selectedOtherApplicationBundle = OtherApplicationsBundle.builder()
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2DocumentReference)
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(selectedOtherApplicationBundle)
                .build()))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        JudicialMessage newMessage = updatedMessages.get(0).getValue();
        List<Element<DocumentReference>> relatedDocuments = newMessage.getRelatedDocuments();

        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            selectedOtherApplicationBundle.getAllDocumentFileNames()
        );
        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToExistingJudicialMessageList() {
        JudicialMessage newMessage = JudicialMessage.builder()
            .subject(MESSAGE_REQUESTED_BY)
            .recipient(MESSAGE_RECIPIENT)
            .sender(MESSAGE_SENDER)
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
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(MESSAGE_NOTE)
            .messageHistory(String.format("%s - %s", MESSAGE_SENDER, MESSAGE_NOTE))
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

        assertThat(messageJudgeService.validateJudgeReplyMessage(caseData))
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

        assertThat(messageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
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

        assertThat(messageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
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

        assertThat(messageJudgeService.validateJudgeReplyMessage(caseData)).isEmpty();
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

        Map<String, Object> updatedData = messageJudgeService.updateJudicialMessages(caseData);
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

        assertThat(messageJudgeService.getNextHearingLabel(caseData))
            .isEqualTo(String.format("Next hearing in the case: %s hearing, %s", hearingType.getLabel(),
                formatLocalDateTimeBaseUsingFormat(hearingStartDate, DATE)));
    }

    @Test
    void shouldNotPopulateFirstHearingLabelWhenHearingDoesNotExists() {
        CaseData caseData = CaseData.builder().build();

        assertThat(messageJudgeService.getNextHearingLabel(caseData)).isEmpty();
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

        Map<String, Object> updatedJudicialMessages = messageJudgeService.updateJudicialMessages(caseData);

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
                element(UUID.randomUUID(), JudicialMessage.builder().build()),
                element(UUID.randomUUID(), JudicialMessage.builder().build())))
            .build();

        assertThatThrownBy(() -> messageJudgeService.updateJudicialMessages(caseData))
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
