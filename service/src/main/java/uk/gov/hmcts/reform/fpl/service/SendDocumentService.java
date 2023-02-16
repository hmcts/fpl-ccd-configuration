package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReferenceWithLanguage;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentService {

    private final SendLetterService sendLetters;
    private final CoreCaseDataService caseService;
    private final SentDocumentHistoryService sentDocuments;

    public void sendDocuments(CaseData caseData, List<DocumentReference> documentToBeSent, List<Recipient> parties) {
        sendDocuments(new SendDocumentRequest(caseData,
            defaultIfNull(documentToBeSent, new ArrayList<DocumentReference>()).stream()
                .map(doc -> DocumentReferenceWithLanguage.builder()
                    .documentReference(doc)
                    .build())
                .collect(toList()),
            parties));
    }

    public void sendDocuments(SendDocumentRequest sendDocumentRequest) {

        List<Recipient> parties = sendDocumentRequest.getParties();
        CaseData caseData = sendDocumentRequest.getCaseData();
        List<DocumentReferenceWithLanguage> documentToBeSent = sendDocumentRequest.getDocumentToBeSent();
        List<Recipient> recipients = defaultIfNull(parties, emptyList());

        List<Recipient> deliverableRecipients = recipients.stream()
            .filter(Recipient::isDeliverable)
            .collect(toList());

        if (recipients.size() != deliverableRecipients.size()) {
            log.error("Case {} has {} recipients with incomplete postal information", caseData.getId(),
                recipients.size() - deliverableRecipients.size());
        }

        if (isNotEmpty(deliverableRecipients) && isNotEmpty(documentToBeSent)) {

            List<SentDocument> docs = documentToBeSent.stream()
                .flatMap(document -> sendLetters.send(document.getDocumentReference(),
                    deliverableRecipients,
                    caseData.getId(),
                    caseData.getFamilyManCaseNumber(),
                    document.getLanguage()).stream())
                .collect(toList());

            List<Element<SentDocuments>> documentsSent = sentDocuments.addToHistory(
                caseData.getDocumentsSentToParties(), docs);

            caseService.updateCase(caseData.getId(), Map.of("documentsSentToParties", documentsSent));
        }
    }

    public List<Recipient> getStandardRecipients(CaseData caseData) {
        final List<Recipient> recipients = new ArrayList<>();

        recipients.addAll(getRepresentativesServedByPost(caseData));
        recipients.addAll(getNotRepresentedRespondents(caseData));

        return recipients;
    }

    private List<Recipient> getRepresentativesServedByPost(CaseData caseData) {
        return new ArrayList<>(caseData.getRepresentativesByServedPreference(POST));
    }

    private List<Recipient> getNotRepresentedRespondents(CaseData caseData) {
        List<Element<Recipient>> partiesWithConfidentialAddress = getPartiesWithConfidentialAddress(caseData);

        if (isNotEmpty(caseData.getRespondents1())) {
            return caseData.getRespondents1().stream()
                .filter(respondent -> ObjectUtils.isEmpty(respondent.getValue().getRepresentedBy())
                    && hasNoLegalRepresentation(respondent.getValue()))
                .filter(respondent -> !respondent.getValue().isDeceasedOrNFA())
                .map(respondent ->
                    ElementUtils.findElement(respondent.getId(), partiesWithConfidentialAddress).isPresent()
                        ? ElementUtils.getElement(respondent.getId(), partiesWithConfidentialAddress).getValue()
                        : respondent.getValue().getParty())
                .collect(toList());
        } else {
            return emptyList();
        }
    }

    private List<Element<Recipient>> getPartiesWithConfidentialAddress(CaseData caseData) {
        if (isNotEmpty(caseData.getRespondents1())) {
            return caseData.getRespondents1().stream()
                .filter(respondent -> respondent.getValue().containsConfidentialDetails()
                    && hasNoLegalRepresentation(respondent.getValue())
                    && containsConfidentialRespondentDetails(caseData.getConfidentialRespondents(), respondent.getId())
                ).map(respondent ->
                    element(respondent.getId(), (Recipient) respondent.getValue().getParty().toBuilder()
                        .address(ElementUtils.getElement(respondent.getId(), caseData.getConfidentialRespondents())
                            .getValue().getParty().getAddress())
                        .build())
                ).collect(toList());
        } else {
            return emptyList();
        }
    }

    private boolean containsConfidentialRespondentDetails(List<Element<Respondent>> confidentialRespondents,
                                                          UUID respondentId) {
        return ElementUtils.findElement(respondentId, confidentialRespondents).isPresent();
    }

    private boolean hasNoLegalRepresentation(Respondent respondent) {
        return !YES.getValue().equals(respondent.getLegalRepresentation());
    }

}
