package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class SentDocumentHistoryService {

    public List<Element<DocumentsSentToParty>> addToHistory(List<Element<DocumentsSentToParty>> sentDocumentsHistory,
                                                            List<SentDocument> printedDocuments) {

        List<Element<DocumentsSentToParty>> historicalRecords = defaultIfNull(sentDocumentsHistory, new ArrayList<>());

        printedDocuments.forEach(printedDocument -> addToHistory(historicalRecords, printedDocument));

        return historicalRecords;
    }

    private void addToHistory(List<Element<DocumentsSentToParty>> documentsSentToPartyCollection,
                              SentDocument printedDocument) {
        DocumentsSentToParty documentsSentToParty = documentsSentToPartyCollection.stream()
            .map(Element::getValue)
            .filter(documentsSent -> documentsSent.getPartyName().equals(printedDocument.getPartyName()))
            .findFirst()
            .orElseGet(() -> createHistoricalRecord(documentsSentToPartyCollection, printedDocument.getPartyName()));

        documentsSentToParty.addDocument(printedDocument);
    }

    private DocumentsSentToParty createHistoricalRecord(List<Element<DocumentsSentToParty>> documentsSentToParties,
                                                        String partyName) {
        DocumentsSentToParty documentsSentToParty = new DocumentsSentToParty(partyName);

        documentsSentToParties.add(element(documentsSentToParty));

        return documentsSentToParty;
    }

}
