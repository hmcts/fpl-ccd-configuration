package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

public class SentDocumentHistoryServiceTest {

    private static final String PARTY_1 = "John Smith";
    private static final String PARTY_2 = "Alex Green";

    SentDocumentHistoryService sentDocumentHistoryService = new SentDocumentHistoryService();

    @Test
    public void shouldCreateHistoricalRecordIfAbsent() {
        SentDocument documentSentToParty1 = sentDocument(PARTY_1);
        SentDocument documentSentToParty2 = sentDocument(PARTY_2);

        List<Element<SentDocuments>> history = new ArrayList<>();
        List<SentDocument> sentDocuments = List.of(documentSentToParty1, documentSentToParty2);

        List<SentDocuments> updatedHistory = unwrapElements(sentDocumentHistoryService
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
    public void shouldCreateHistoricalRecordWithMultipleDocuments() {
        SentDocument document1SentToParty = sentDocument(PARTY_1);
        SentDocument document2SentToParty = sentDocument(PARTY_1);

        List<Element<SentDocuments>> history = new ArrayList<>();
        List<SentDocument> sentDocuments = List.of(document1SentToParty, document2SentToParty);

        List<SentDocuments> updatedHistory = unwrapElements(sentDocumentHistoryService
            .addToHistory(history, sentDocuments));

        assertThat(updatedHistory).hasSize(1);

        assertThat(updatedHistory.get(0).getPartyName()).isEqualTo(PARTY_1);
        assertThat(unwrapElements(updatedHistory.get(0).getDocumentsSentToParty()))
            .containsExactly(document1SentToParty, document2SentToParty);
    }

    @Test
    public void shouldUpdateHistoricalRecordIfPresent() {
        SentDocument document1SentToParty1 = sentDocument(PARTY_1);
        SentDocument document2SentToParty1 = sentDocument(PARTY_1);
        SentDocument document1SentToParty2 = sentDocument(PARTY_2);

        SentDocuments documentsSentToParty1 = sentDocumentsHistory(PARTY_1, document1SentToParty1);
        SentDocuments documentsSentToParty2 = sentDocumentsHistory(PARTY_2, document1SentToParty2);

        List<Element<SentDocuments>> history = wrapElements(documentsSentToParty1, documentsSentToParty2);
        List<SentDocument> sentDocuments = List.of(document2SentToParty1);

        List<SentDocuments> updatedHistory = unwrapElements(sentDocumentHistoryService
            .addToHistory(history, sentDocuments));

        assertThat(updatedHistory).hasSize(2);

        assertThat(updatedHistory.get(0).getPartyName()).isEqualTo(PARTY_1);
        assertThat(unwrapElements(updatedHistory.get(0).getDocumentsSentToParty()))
            .containsExactly(document1SentToParty1, document2SentToParty1);

        assertThat(updatedHistory.get(1)).isEqualTo(documentsSentToParty2);
    }

    private static SentDocument sentDocument(String partyName) {
        return SentDocument.builder()
            .partyName(partyName)
            .document(testDocumentReference())
            .build();
    }

    private static SentDocuments sentDocumentsHistory(String partyName, SentDocument... sentDocuments) {
        return SentDocuments.builder()
            .partyName(partyName)
            .documentsSentToParty(wrapElements(sentDocuments))
            .build();
    }
}
