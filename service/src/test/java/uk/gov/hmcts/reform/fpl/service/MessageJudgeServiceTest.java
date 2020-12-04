package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class MessageJudgeServiceTest {
    private Time time = new FixedTimeConfiguration().stoppedTime();
    private IdentityService identityService = new IdentityService();

    private MessageJudgeService messageJudgeService = new MessageJudgeService(time, identityService);

    @Test
    void shouldInitialiseC2CaseFields() {
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
    void shouldRebuildC2DynamicListAndFormatC2DocumentsCorrectly() {
        UUID selectedC2Id = UUID.randomUUID();

        DocumentReference mainC2Document = DocumentReference.builder()
            .filename("c2.doc")
            .build();

        DocumentReference supportingC2Document = DocumentReference.builder()
            .filename("additional_c2_document.doc")
            .build();

        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(mainC2Document)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2Document)
                    .build())))
            .build();

        CaseData caseData = CaseData.builder()
            .c2DynamicList(selectedC2Id)
            .c2DocumentBundle(List.of(
                element(selectedC2Id, selectedC2DocumentBundle),
                element(UUID.randomUUID(), C2DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("other_c2.doc")
                        .build())
                    .build())
            ))
            .build();

        String expectedC2Label = mainC2Document + "\n" + supportingC2Document;

        assertThat(messageJudgeService.buildRelatedC2DocumentFields(caseData, selectedC2Id))
            .extracting("relatedDocumentsLabel", "c2DynamicList")
            .containsExactly(expectedC2Label, caseData.buildC2DocumentDynamicList(selectedC2Id));
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageList() {
        String note = "Some note";
        String sender = "sender@fpla.com";
        String recipient = "judge@fpla.com";
        String relatedDocuments = "doc1.doc";

        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .sender(sender)
            .recipient(recipient)
            .build();

        CaseData caseData = CaseData.builder()
            .relatedDocumentsLabel(relatedDocuments)
            .judicialMessageNote(note)
            .judicialMessageMetaData(judicialMessageMetaData)
            .build();

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        JudicialMessage expectedJudicialMessage = JudicialMessage.builder()
            .dateSentAsLocalDateTime(time.now())
            .status(OPEN)
            .note(note)
            .dateSent(formatLocalDateToString(time.now().toLocalDate(), "d MMMM yyyy"))
            .relatedDocuments(relatedDocuments)
            .sender(sender)
            .recipient(recipient)
            .build();

        assertThat(updatedMessages).hasSize(1).first()
            .extracting(Element::getValue).isEqualTo(expectedJudicialMessage);
    }

    @Test
    void shouldAppendNewJudicialMessageToExistingJudicialMessageList() {
        String note = "Some note";
        String sender = "sender@fpla.com";
        String recipient = "judge@fpla.com";

        JudicialMessage newMessage = JudicialMessage.builder()
            .sender(sender)
            .recipient(recipient)
            .build();

        List<Element<JudicialMessage>> existingJudicialMessages = new ArrayList<>();
        existingJudicialMessages.add(element(JudicialMessage.builder().build()));

        CaseData caseData = CaseData.builder()
            .judicialMessages(existingJudicialMessages)
            .judicialMessageNote(note)
            .judicialMessageMetaData(newMessage)
            .build();

        List<Element<JudicialMessage>> updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);

        assertThat(updatedMessages.size()).isEqualTo(2);
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
