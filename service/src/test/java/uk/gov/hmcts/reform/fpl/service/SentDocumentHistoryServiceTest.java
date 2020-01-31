package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParty;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

public class SentDocumentHistoryServiceTest {

    private static final String PARTY_1 = "John Smith";
    private static final String PARTY_2 = "Alex Green";

    SentDocumentHistoryService sentDocumentHistoryService = new SentDocumentHistoryService();

    @Test
    void shouldCreateHistoricalRecordIfAbsent() {
        DocumentSentToParty documentSentToParty1 = sentDocument(PARTY_1);
        DocumentSentToParty documentSentToParty2 = sentDocument(PARTY_2);

        List<Element<DocumentsSentToParty>> history = new ArrayList<>();
        List<DocumentSentToParty> sentDocuments = List.of(documentSentToParty1, documentSentToParty2);

        List<DocumentsSentToParty> updatedHistory = unwrapElements(sentDocumentHistoryService
            .addToHistory(history, sentDocuments));

        assertThat(updatedHistory).hasSize(2);

        assertThat(updatedHistory.get(0).getPartyName()).isEqualTo(PARTY_1);
        assertThat(unwrapElements(updatedHistory.get(0).getDocumentsSentToParty()))
            .containsExactly(documentSentToParty1);

        assertThat(updatedHistory.get(1).getPartyName()).isEqualTo(PARTY_2);
        assertThat(unwrapElements(updatedHistory.get(1).getDocumentsSentToParty()))
            .containsExactly(documentSentToParty2);
    }

    @Test
    void shouldCreateHistoricalRecordWithMultipleDocuments() {
        DocumentSentToParty document1SentToParty = sentDocument(PARTY_1);
        DocumentSentToParty document2SentToParty = sentDocument(PARTY_1);

        List<Element<DocumentsSentToParty>> history = new ArrayList<>();
        List<DocumentSentToParty> sentDocuments = List.of(document1SentToParty, document2SentToParty);

        List<DocumentsSentToParty> updatedHistory = unwrapElements(sentDocumentHistoryService
            .addToHistory(history, sentDocuments));

        assertThat(updatedHistory).hasSize(1);

        assertThat(updatedHistory.get(0).getPartyName()).isEqualTo(PARTY_1);
        assertThat(unwrapElements(updatedHistory.get(0).getDocumentsSentToParty()))
            .containsExactly(document1SentToParty, document2SentToParty);
    }

    @Test
    void shouldUpdateHistoricalRecordIfPresent() {
        DocumentSentToParty document1SentToParty1 = sentDocument(PARTY_1);
        DocumentSentToParty document2SentToParty1 = sentDocument(PARTY_1);
        DocumentSentToParty document1SentToParty2 = sentDocument(PARTY_2);

        DocumentsSentToParty documentsSentToParty1 = sentDocumentsHistory(PARTY_1, document1SentToParty1);
        DocumentsSentToParty documentsSentToParty2 = sentDocumentsHistory(PARTY_2, document1SentToParty2);

        List<Element<DocumentsSentToParty>> history = wrapElements(documentsSentToParty1, documentsSentToParty2);
        List<DocumentSentToParty> sentDocuments = List.of(document2SentToParty1);

        List<DocumentsSentToParty> updatedHistory = unwrapElements(sentDocumentHistoryService
            .addToHistory(history, sentDocuments));

        assertThat(updatedHistory).hasSize(2);

        assertThat(updatedHistory.get(0).getPartyName()).isEqualTo(PARTY_1);
        assertThat(unwrapElements(updatedHistory.get(0).getDocumentsSentToParty()))
            .containsExactly(document1SentToParty1, document2SentToParty1);

        assertThat(updatedHistory.get(1)).isEqualTo(documentsSentToParty2);
    }

    private static DocumentSentToParty sentDocument(String partyName) {
        return DocumentSentToParty.builder()
            .partyName(partyName)
            .document(testDocument())
            .build();
    }

    private static DocumentsSentToParty sentDocumentsHistory(String partyName, DocumentSentToParty... sentDocuments) {
        return DocumentsSentToParty.builder()
            .partyName(partyName)
            .documentsSentToParty(wrapElements(sentDocuments))
            .build();
    }
}
