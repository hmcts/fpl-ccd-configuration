package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class MessageJudgeServiceTest {
    private Time time = new FixedTimeConfiguration().stoppedTime();

    private ObjectMapper mapper = new ObjectMapper();

    private IdentityService identityService = mock(IdentityService.class);

    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final UUID SELECTED_C2_ID = UUID.randomUUID();
    private static final UUID NEW_ELEMENT_ID = UUID.randomUUID();

    private MessageJudgeService messageJudgeService = new MessageJudgeService(time, identityService, mapper);

    @Test
    void shouldInitialiseC2CaseFieldsWhenC2DocumentsExist() {
        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(element(C2DocumentBundle.builder()
                .uploadedDateTime("01 Dec 2020")
                .author("Some author")
                .build())))
            .build();

        Map<String, Object> expectedData = Map.of(
            "hasC2Applications", YES.getValue(),
            "c2DynamicList", caseData.buildC2DocumentDynamicList()
        );

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
                    .code(SELECTED_C2_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .c2DocumentBundle(List.of(
                element(SELECTED_C2_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = C2_FILE_NAME + "\n" + C2_SUPPORTING_DOCUMENT_FILE_NAME;

        assertThat(messageJudgeService.buildRelatedC2DocumentFields(caseData))
            .extracting("relatedDocumentsLabel", "c2DynamicList")
            .containsExactly(expectedC2Label, caseData.buildC2DocumentDynamicList(SELECTED_C2_ID));
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
                element(SELECTED_C2_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        assertThat(messageJudgeService.buildRelatedC2DocumentFields(caseData)).isEmpty();
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenC2DocumentNotSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .build();

        CaseData caseData = CaseData.builder().messageJudgeEventData(messageJudgeEventData).build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        Element<JudicialMessage> expectedJudicialMessageElement = element(NEW_ELEMENT_ID, JudicialMessage.builder()
            .dateSentAsLocalDateTime(time.now())
            .status(OPEN)
            .note(MESSAGE_NOTE)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .build());

        assertThat(updatedMessages).hasSize(1).first().isEqualTo(expectedJudicialMessageElement);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenC2DocumentHasBeenSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .sender(MESSAGE_SENDER)
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
                    .code(SELECTED_C2_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .c2DocumentBundle(List.of(
                element(SELECTED_C2_ID, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        List<Element<DocumentReference>> relatedDocuments = updatedMessages.get(0).getValue().getRelatedDocuments();

        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToExistingJudicialMessageList() {
        JudicialMessage newMessage = JudicialMessage.builder()
            .sender(MESSAGE_SENDER)
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
            .dateSentAsLocalDateTime(time.now())
            .status(OPEN)
            .note(MESSAGE_NOTE)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        assertThat(updatedMessages.size()).isEqualTo(2);
        assertThat(updatedMessages).isEqualTo(List.of(
            element(existingJudicialMessageId, existingJudicialMessage),
            element(NEW_ELEMENT_ID, expectedNewJudicialMessage)
        ));
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

    private Element<JudicialMessage> buildJudicialMessageElement(LocalDateTime dateTime) {
        return element(JudicialMessage.builder().dateSentAsLocalDateTime(dateTime).build());
    }
}
