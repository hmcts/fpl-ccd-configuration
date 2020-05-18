package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class SentDocumentHistoryService {

    public List<Element<SentDocuments>> addToHistory(List<Element<SentDocuments>> sentDocumentsHistory,
                                                     List<SentDocument> printedDocuments) {

        List<Element<SentDocuments>> historicalRecords = defaultIfNull(sentDocumentsHistory, new ArrayList<>());

        printedDocuments.forEach(printedDocument -> addToHistory(historicalRecords, printedDocument));

        return historicalRecords;
    }

    private void addToHistory(List<Element<SentDocuments>> documentsSentToPartyCollection,
                              SentDocument printedDocument) {
        SentDocuments documentsSentToParty = documentsSentToPartyCollection.stream()
            .map(Element::getValue)
            .filter(documentsSent -> documentsSent.getPartyName().equals(printedDocument.getPartyName()))
            .findFirst()
            .orElseGet(() -> createHistoricalRecord(documentsSentToPartyCollection, printedDocument.getPartyName()));

        documentsSentToParty.addDocument(printedDocument);
    }

    private SentDocuments createHistoricalRecord(List<Element<SentDocuments>> documentsSentToParties,
                                                 String partyName) {
        SentDocuments documentsSentToParty = new SentDocuments(partyName);

        documentsSentToParties.add(element(documentsSentToParty));

        return documentsSentToParty;
    }

}
