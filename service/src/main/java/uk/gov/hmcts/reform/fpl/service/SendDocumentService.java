package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendDocumentService {

    private final SendLetterService sendLetters;
    private final CoreCaseDataService caseService;
    private final SentDocumentHistoryService sentDocuments;
    private final OthersService othersService;

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

    public List<Recipient> getSelectedOtherRecipients(CaseData caseData, List<Element<Other>> othersSelected) {
        List<Recipient> recipients = new ArrayList<>();
        List<Representative> otherRepresentatives = new ArrayList<>();

        othersSelected.stream().forEach(other -> {
            Other otherToBeNotified = other.getValue();
            if ((othersService.isRepresented(otherToBeNotified))) {
                otherToBeNotified.getRepresentedBy().stream().forEach(representedBy -> {
                    Optional<Representative> representative = caseData.getRepresentatives().stream()
                        .filter(element -> element.getId().equals(representedBy.getValue()) && element.getValue()
                            .getServingPreferences() == POST)
                        .map(Element::getValue)
                        .findFirst();

                    if (representative.isPresent()) {
                        otherRepresentatives.add(representative.get());
                    }
                });

            } else {
                if (othersService.hasAddressAdded(otherToBeNotified)) {
                    recipients.add(Representative.builder()
                        .fullName(otherToBeNotified.getName())
                        .address(otherToBeNotified.getAddress())
                        .build());
                }
            }
        });

        recipients.addAll(new ArrayList<>((otherRepresentatives)));

        return recipients;
    }

    public List<Recipient> getRecipientsExcludingOthers(CaseData caseData) {
        final List<Recipient> recipients = new ArrayList<>();

        List<Representative> representatives = caseData.getRepresentativesByServedPreference(POST)
            .stream()
            .filter(representative -> !representative.getRole().getType()
                .equals(RepresentativeRole.Type.OTHER))
            .collect(Collectors.toList());

        recipients.addAll(new ArrayList<>((representatives)));
        recipients.addAll(getNotRepresentedRespondents(caseData));

        return recipients;
    }

    private List<Recipient> getRepresentativesServedByPost(CaseData caseData) {
        return new ArrayList<>(caseData.getRepresentativesByServedPreference(POST));
    }

    private List<Recipient> getNotRepresentedRespondents(CaseData caseData) {
        return unwrapElements(caseData.getRespondents1()).stream()
            .filter(respondent -> ObjectUtils.isEmpty(respondent.getRepresentedBy())
                && hasNoLegalRepresentation(respondent))
            .map(Respondent::getParty)
            .collect(toList());
    }

    private boolean hasNoLegalRepresentation(Respondent respondent) {
        return !YES.getValue().equals(respondent.getLegalRepresentation());
    }

}
