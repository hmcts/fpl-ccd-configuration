package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
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
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService.UPDATE_CASE_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentService {

    private final SendLetterService sendLetters;
    private final CoreCaseDataService caseService;
    private final SentDocumentHistoryService sentDocuments;
    private final CaseConverter caseConverter;

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
            // Perform sendLetter calls
            List<SentDocument> docs = documentToBeSent.stream()
                .flatMap(document -> sendLetters.send(document.getDocumentReference(),
                    deliverableRecipients,
                    caseData.getId(),
                    caseData.getFamilyManCaseNumber(),
                    document.getLanguage()).stream())
                .collect(toList());

            // Pop the audit trail on the case data if successful
            caseService.performPostSubmitCallback(caseData.getId(), UPDATE_CASE_EVENT,
                caseDetails -> {
                    CaseData currentCaseData = caseConverter.convert(caseDetails);
                    List<Element<SentDocuments>> documentsSent = sentDocuments.addToHistory(
                        currentCaseData.getDocumentsSentToParties(), docs);

                    return Map.of("documentsSentToParties", documentsSent);
                });
        }
    }

    public List<Recipient> getStandardRecipients(CaseData caseData) {
        final List<Recipient> recipients = new ArrayList<>();

        recipients.addAll(getRepresentativesServedByPost(caseData));
        recipients.addAll(getNotRepresentedRespondents(caseData));

        return recipients;
    }

    public List<Recipient> getRepresentativesServedByPost(CaseData caseData) {
        return new ArrayList<>(caseData.getRepresentativesByServedPreference(POST));
    }

    private List<Recipient> getNotRepresentedRespondents(CaseData caseData) {
        if (isNotEmpty(caseData.getRespondents1())) {
            return caseData.getRespondents1().stream()
                .filter(respondent -> ObjectUtils.isEmpty(respondent.getValue().getRepresentedBy())
                    && hasNoLegalRepresentation(respondent.getValue()))
                .filter(respondent -> !respondent.getValue().isDeceasedOrNFA())
                .filter(respondent -> isRespondentAddressKnown(respondent, caseData))
                .map(respondent -> respondent.getValue().getParty().toBuilder()
                        .address(getPartyAddress(respondent, caseData)).build())
                .collect(toList());
        }

        return emptyList();
    }

    private boolean isRespondentAddressKnown(Element<Respondent> respondent, CaseData caseData) {
        if (respondent.getValue().containsConfidentialDetails()) {

            return Optional.ofNullable(ElementUtils.getElement(respondent.getId(),
                    caseData.getConfidentialRespondents()))
                .map(Element::getValue)
                .map(Respondent::getParty)
                .map(RespondentParty::getAddressKnow)
                .map(val -> val != IsAddressKnowType.NO)
                .orElse(true);
        }

        return true;
    }

    private Address getPartyAddress(Element<Respondent> respondent, CaseData caseData) {
        if (respondent.getValue().containsConfidentialDetails()) {
            return ElementUtils.getElement(respondent.getId(), caseData.getConfidentialRespondents())
                 .getValue().getParty().getAddress();
        }

        return respondent.getValue().getParty().getAddress();
    }

    private boolean hasNoLegalRepresentation(Respondent respondent) {
        return !YES.getValue().equals(respondent.getLegalRepresentation());
    }
}
