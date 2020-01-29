package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.PrintedDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class DocumentHistoryService {

    public List<Element<DocumentsSentToParty>> updateDocumentsSentToPartyCollection(
        List<PrintedDocument> printedDocuments, List<Element<DocumentsSentToParty>> documentsSentToPartyCollection) {
        List<Element<DocumentsSentToParty>> updatedDocumentsSent = defaultIfNull(documentsSentToPartyCollection,
            new ArrayList<>());

        for (PrintedDocument printedDocument : printedDocuments) {
            Optional<Element<DocumentsSentToParty>> documentSentToParty = getDocumentSentToParty(updatedDocumentsSent,
                printedDocument.getRepresentativeName());
            Element<DocumentsSentToParty> documentsSentToPartyElement = documentSentToParty
                .orElse(partyDocuments(printedDocument.getRepresentativeName()));
            documentsSentToPartyElement.getValue().addDocument(printedDocument);
            if (documentSentToParty.isEmpty()) {
                updatedDocumentsSent.add(documentsSentToPartyElement);
            }
        }

        return updatedDocumentsSent;
    }

    private Optional<Element<DocumentsSentToParty>> getDocumentSentToParty(
        List<Element<DocumentsSentToParty>> documentsSentToPartyCollection, String representativeName) {
        return documentsSentToPartyCollection.stream().filter(
            documentsSentToPartyElement -> documentsSentToPartyElement.getValue().getPartyName().equals(
                representativeName)).findFirst();
    }

    private Element<DocumentsSentToParty> partyDocuments(String partyName) {
        return element(DocumentsSentToParty.builder()
            .partyName(partyName)
            .build());
    }

}
