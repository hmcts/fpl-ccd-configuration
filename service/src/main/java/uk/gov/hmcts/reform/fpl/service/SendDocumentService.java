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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentService {

    private final SendLetterService sendLetters;
    private final CoreCaseDataService caseService;
    private final SentDocumentHistoryService sentDocuments;

    public void sendDocuments(CaseData caseData, List<DocumentReference> documentToBeSent, List<Recipient> parties) {

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
                .flatMap(document -> sendLetters.send(document,
                    deliverableRecipients,
                    caseData.getId(),
                    caseData.getFamilyManCaseNumber()).stream())
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
        return unwrapElements(caseData.getRespondents1()).stream()
            .filter(respondent -> ObjectUtils.isEmpty(respondent.getRepresentedBy()))
            .map(Respondent::getParty)
            .collect(toList());
    }

}
