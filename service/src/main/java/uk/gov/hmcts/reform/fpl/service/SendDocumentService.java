package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.getExtension;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentService {

    private final SendLetterService sendLetters;
    private final CoreCaseDataService caseService;
    private final SentDocumentHistoryService sentDocuments;

    public List<Recipient> getStandardRecipients(CaseData caseData) {
        final List<Recipient> recipients = new ArrayList<>();

        recipients.addAll(getRepresentativesServedByPost(caseData));
        recipients.addAll(getNotRepresentedRespondents(caseData));

        return recipients;
    }

    public void sendDocuments(CaseData caseData, List<DocumentReference> documentToBeSent, List<Recipient> parties) {
        sendDocuments(new SendDocumentRequest(
            caseData,
            nullSafeList(documentToBeSent).stream()
                .map(doc -> DocumentReferenceWithLanguage.builder().documentReference(doc).build())
                .collect(toList()),
            parties
        ));
    }

    public void sendDocuments(SendDocumentRequest sendDocumentRequest) {
        List<Recipient> parties = sendDocumentRequest.getParties();
        CaseData caseData = sendDocumentRequest.getCaseData();
        List<DocumentReferenceWithLanguage> documentToBeSent = sendDocumentRequest.getDocumentToBeSent();
        List<Recipient> recipients = nullSafeList(parties);

        List<Recipient> deliverableRecipients = recipients.stream().filter(Recipient::isDeliverable).collect(toList());

        Long caseId = caseData.getId();
        if (recipients.size() != deliverableRecipients.size()) {
            log.error(
                "Case {} has {} recipients with incomplete postal information",
                caseId, recipients.size() - deliverableRecipients.size()
            );
        }

        if (isNotEmpty(deliverableRecipients) && isNotEmpty(documentToBeSent)) {
            String caseNumber = caseData.getFamilyManCaseNumber();
            List<SentDocument> docs = documentToBeSent.stream()
                .flatMap(document -> sendDocument(deliverableRecipients, document, caseNumber, caseId))
                .collect(toList());

            List<Element<SentDocuments>> documentsSent = sentDocuments.addToHistory(
                caseData.getDocumentsSentToParties(), docs
            );

            caseService.updateCase(caseId, Map.of("documentsSentToParties", documentsSent));
        }
    }

    private Stream<SentDocument> sendDocument(List<Recipient> recipients, DocumentReferenceWithLanguage document,
                                              String caseNumber, Long id) {
        DocumentReference reference = document.getDocumentReference();
        try {
            return sendLetters.send(reference, recipients, id, caseNumber, document.getLanguage()).stream();
        } catch (Exception e) {
            String extension = getExtension(reference);
            log.error(
                "Not sending document of type {} for case {} due to \"{}\"", extension, id, e.getMessage(), e
            );
            return Stream.empty();
        }
    }

    private List<Recipient> getRepresentativesServedByPost(CaseData caseData) {
        return new ArrayList<>(caseData.getRepresentativesByServedPreference(POST));
    }

    private List<Recipient> getNotRepresentedRespondents(CaseData caseData) {
        return unwrapElements(caseData.getRespondents1()).stream()
            .filter(respondent -> isEmpty(respondent.getRepresentedBy()) && hasNoLegalRepresentation(respondent))
            .map(Respondent::getParty)
            .collect(toList());
    }

    private boolean hasNoLegalRepresentation(Respondent respondent) {
        return !YES.getValue().equals(respondent.getLegalRepresentation());
    }

}
