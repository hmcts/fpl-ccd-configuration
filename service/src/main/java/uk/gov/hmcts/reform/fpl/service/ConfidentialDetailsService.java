package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ConfidentialDetailsService {

    public List<Element<Other>> combineOtherDetails(List<Element<Other>> all, List<Element<Other>> confidential) {
        return prepareCollection(all, confidential, null);
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> getConfidentialDetails(List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .filter(element -> element.getValue().containsConfidentialDetails())
            .map(item -> element(item.getId(), item.getValue().extractConfidentialDetails()))
            .collect(toList());
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> removeConfidentialDetails(List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .map(item -> {
                if (item.getValue().containsConfidentialDetails()) {
                    return element(item.getId(), item.getValue().removeConfidentialDetails());
                } else {
                    return item;
                }
            })
            .collect(toList());
    }

    public <T extends ConfidentialParty<T>> void addConfidentialDetailsToCase(CaseDetails caseDetails,
                                                                              List<Element<T>> all,
                                                                              ConfidentialPartyType type) {
        List<Element<T>> confidentialDetails = getConfidentialDetails(all);

        if (isNotEmpty(confidentialDetails)) {
            if (!type.equals(OTHER)) {
                caseDetails.getData().put(type.getCaseDataKey(), removeConfidentialDetails(all));
            }
            caseDetails.getData().put(type.getConfidentialKey(), confidentialDetails);
        } else {
            caseDetails.getData().remove(type.getCaseDataKey());
        }
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> prepareCollection(List<Element<T>> all,
                                                                               List<Element<T>> confidential,
                                                                               T defaultValue) {
        List<Element<T>> collection = new ArrayList<>();

        if (all.isEmpty()) {
            collection.add(element(defaultValue));

        } else if (confidential.isEmpty()) {
            return all;

        } else {
            all.forEach(element -> {
                T party = element.getValue();
                if (party.containsConfidentialDetails()) {
                    T partyToAdd = getItemToAdd(confidential, element);
                    T confidentialParty = party.addConfidentialDetails((partyToAdd.toParty()));

                    // code due to others following a different data structure.
                    if (defaultValue == null) {
                        confidentialParty = handleOthers(all, confidential, party);
                    }

                    collection.add(element(element.getId(), confidentialParty));
                } else {
                    collection.add(element);
                }
            });
        }
        return collection;
    }

    private <T extends ConfidentialParty<T>> T handleOthers(List<Element<T>> all,
                                                            List<Element<T>> confidential,
                                                            T party) {
        T confidentialParty;
        List<UUID> ids = all.stream().map(Element::getId).collect(toList());

        confidentialParty = confidential.stream()
            .filter(other -> !ids.contains(other.getId()))
            .map(other -> party.addConfidentialDetails(other.getValue().toParty()))
            .findFirst()
            .orElse(party);
        return confidentialParty;
    }

    private <T> T getItemToAdd(List<Element<T>> confidential, Element<T> element) {
        return confidential.stream()
            .filter(item -> item.getId().equals(element.getId()))
            .map(Element::getValue)
            .findFirst()
            .orElse(element.getValue());
    }
}
