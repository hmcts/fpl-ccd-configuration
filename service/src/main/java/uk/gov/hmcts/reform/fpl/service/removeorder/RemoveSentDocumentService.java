package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableSentDocumentNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationRemovalReason.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveSentDocumentService {

    public DynamicList buildDynamicList(CaseData caseData) {
        return buildDynamicList(caseData, null);
    }

    public DynamicList buildDynamicList(CaseData caseData, UUID selected) {
        List<Element<SentDocuments>> documentsByParty = defaultIfNull(
            caseData.getDocumentsSentToParties(), new ArrayList<>());

        documentsByParty.sort(Comparator
            .comparing((Element<SentDocuments> bundle) -> bundle.getValue().getPartyName()));

        List<Element<SentDocument>> allDocuments = new ArrayList<>();
        unwrapElements(documentsByParty).stream().forEach(d -> {
            d.getDocumentsSentToParty().sort(Comparator
                .comparing((Element<SentDocument> bundle) -> bundle.getValue().getDocument().getFilename()));
            allDocuments.addAll(d.getDocumentsSentToParty());
        });

        return asDynamicList(allDocuments, selected, (r) ->
            format("%s - %s", r.getPartyName(), format("%s (%s)", r.getDocument().getFilename(), r.getSentAt())));
    }


    public Element<SentDocument> getRemovedSentDocumentById(CaseData caseData, UUID selectedDocId) {
        return caseData.getDocumentsSentToParties().stream()
            .map(c -> c.getValue().getDocumentsSentToParty())
            .flatMap(Collection::stream)
            .collect(Collectors.toList()).stream().filter(document -> selectedDocId.equals(document.getId()))
            .findAny()
            .orElseThrow(() -> new RemovableSentDocumentNotFoundException(selectedDocId));
    }

    public void populateSentDocumentFields(CaseDetailsMap data, SentDocument sentDocument) {
        data.put("partyNameToBeRemoved", defaultIfNull(sentDocument.getPartyName(), ""));
        data.put("sentDocumentToBeRemoved", sentDocument.getDocument());
        data.put("sentAtToBeRemoved", sentDocument.getSentAt());
        data.put("letterIdToBeRemoved", sentDocument.getLetterId());
    }

    public void removeSentDocumentFromCase(CaseData caseData, CaseDetailsMap caseDetailsMap,
                                           UUID removedDocId) {
        Element<SentDocument> removedSentDocument = getRemovedSentDocumentById(caseData, removedDocId);
        Element<SentDocuments>  removedSentDocuments = caseData.getDocumentsSentToParties().stream()
            .filter(s -> s.getValue().getDocumentsSentToParty().stream().filter(e -> e.equals(removedSentDocument))
                .findFirst().isPresent())
            .findFirst().get();

        caseData.getDocumentsSentToParties().stream().filter(p ->
                p.getValue().getDocumentsSentToParty().contains(removedSentDocument))
            .findFirst()
            .orElseThrow(() -> new RemovableSentDocumentNotFoundException(removedDocId)).getValue()
            .getDocumentsSentToParty().remove(removedSentDocument);

        caseDetailsMap.putIfNotEmpty("documentsSentToParties", caseData.getDocumentsSentToParties());

        removedSentDocument.getValue().setRemovalReason(getReasonToRemove(caseData));
        List<Element<SentDocuments>> hiddenDocumentsSentToParties = caseData.getRemovalToolData()
                .getHiddenDocumentsSentToParties();
        Optional<Element<SentDocuments>> existingHiddenSentDocuments =
            ElementUtils.findElement(removedSentDocuments.getId(), hiddenDocumentsSentToParties);
        if (!existingHiddenSentDocuments.isPresent()) {
            hiddenDocumentsSentToParties.add(element(removedSentDocuments.getId(), SentDocuments.builder()
                .partyName(removedSentDocuments.getValue().getPartyName()).build()));
            existingHiddenSentDocuments =
                ElementUtils.findElement(removedSentDocuments.getId(), hiddenDocumentsSentToParties);
        }
        existingHiddenSentDocuments.get().getValue().getDocumentsSentToParty().add(removedSentDocument);
        caseDetailsMap.put("hiddenDocumentsSentToParties", hiddenDocumentsSentToParties);
    }

    private String getReasonToRemove(CaseData caseData) {
        return caseData.getRemovalToolData().getReasonToRemoveSentDocument();
    }

}
