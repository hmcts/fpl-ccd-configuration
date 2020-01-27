package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.DocumentSent;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParties;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
public class DocumentHistoryService {

    public List<Element<DocumentsSentToParty>> addDocumentSentToParties(DocumentSentToParties documentSentToParties, List<Element<DocumentsSentToParty>> documentsSent) {
        List<Element<DocumentsSentToParty>> updatedDocumentsSent = defaultIfNull(documentsSent, new ArrayList<>());

        documentSentToParties.getParties().forEach(partyName ->
            updatedDocumentsSent.stream()
                .map(Element::getValue)
                .filter(document -> document.getPartyName().equals(partyName))
                .findFirst()
                .ifPresentOrElse(
                    partyDocuments -> partyDocuments.addDocument(documentSentToParties.getDocument()),
                    () -> updatedDocumentsSent.add(partyDocuments(partyName, documentSentToParties.getDocument()))));

        return updatedDocumentsSent;
    }

    private Element<DocumentsSentToParty> partyDocuments(String partyName, DocumentSent documentSent) {
        return element(DocumentsSentToParty.builder()
            .partyName(partyName)
            .documentsSentToParty(wrapElements(documentSent))
            .build());
    }

}
