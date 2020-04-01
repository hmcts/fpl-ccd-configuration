package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ConfidentialDetailsService {

    public List<Element<Child>> combineChildDetails(List<Element<Child>> all, List<Element<Child>> confidential) {
        return prepareCollection(all, confidential, expandedChild());
    }

    public List<Element<Respondent>> combineRespondentDetails(List<Element<Respondent>> all,
                                                              List<Element<Respondent>> confidential) {
        return prepareCollection(all, confidential, expandedRespondent());
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> getConfidentialDetails(List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .filter(element -> element.getValue().containsConfidentialDetails())
            .map(item -> element(item.getId(), item.getValue().getConfidentialDetails()))
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

    public <T> void addConfidentialDetailsToCase(CaseDetails caseDetails,
                                                 List<Element<T>> confidentialDetails,
                                                 ConfidentialPartyType type) {
        if (isNotEmpty(confidentialDetails)) {
            caseDetails.getData().put(type.getCaseDataKey(), confidentialDetails);
        } else {
            caseDetails.getData().remove(type.getCaseDataKey());
        }
    }

    private Child expandedChild() {
        return Child.builder().build().expandCollection();
    }

    private Respondent expandedRespondent() {
        return Respondent.builder().build().expandCollection();
    }

    private <T extends ConfidentialParty<T>> List<Element<T>> prepareCollection(List<Element<T>> all,
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
                    T confidentialParty = party.addConfidentialDetails((partyToAdd.getConfidentialParty()));
                    collection.add(element(element.getId(), confidentialParty));
                } else {
                    collection.add(element);
                }
            });
        }
        return collection;
    }

    private <T> T getItemToAdd(List<Element<T>> confidential, Element<T> element) {
        return confidential.stream()
            .filter(item -> item.getId().equals(element.getId()))
            .map(Element::getValue)
            .findFirst()
            .orElse(element.getValue());
    }
}
