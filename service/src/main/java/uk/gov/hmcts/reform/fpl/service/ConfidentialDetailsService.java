package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ConfidentialDetailsService {

    public <T extends ConfidentialParty<T>> List<Element<T>> addPartyMarkedConfidentialToList(
        List<Element<T>> details) {
        return details.stream()
            .filter(element -> element.getValue() != null)
            .filter(element -> element.getValue().containsConfidentialDetails())
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

    public <T extends ConfidentialParty<T>> List<Element<T>> retainConfidentialDetails(
        List<Element<T>> list, Supplier<T> builder) {
        List<Element<T>> modified = new ArrayList<>();

        list.forEach(element -> {
            Party party = element.getValue().getConfidentialParty();
            T t = builder.get();
            t = t.cloneWithConfidentialParty(party);
            modified.add(element(element.getId(), t));
        });

        return modified;
    }

    public List<Element<Child>> prepareConfidentialChildCollection(List<Element<Child>> all,
                                                                   List<Element<Child>> confidential) {

        return prepareConfidentialCollection(all, confidential, defaultChild());
    }

    public List<Element<Respondent>> prepareConfidentialRespondentCollection(List<Element<Respondent>> all,
                                                                             List<Element<Respondent>> confidential) {

        return prepareConfidentialCollection(all, confidential, defaultRespondent());
    }

    public <T extends ConfidentialParty<T>> List<Element<T>> prepareConfidentialCollection(
        List<Element<T>> all, List<Element<T>> confidential, T defaultElement) {

        checkNotNull(defaultElement);
        List<Element<T>> collection = new ArrayList<>();
        if (isEmpty(all.isEmpty())) {
            collection.add(element(defaultElement));
            return collection;
        } else if (isEmpty(confidential)) {
            return all;
        } else {
            all.forEach(element -> {
                T base = element.getValue();
                if (base.containsConfidentialDetails()) {
                    T confidentialItem = getConfidentialItem(confidential, element);
                    T cloned = base.cloneWithFullParty(confidentialItem.getConfidentialParty());
                    collection.add(element(element.getId(), cloned));
                } else {
                    collection.add(element);
                }
            });
            return collection;
        }
    }

    private <T> T getConfidentialItem(List<Element<T>> elements, Element<T> toFind) {
        return elements.stream()
            .filter(element -> element.getId().equals(toFind.getId()))
            .map(Element::getValue)
            .findFirst()
            .orElse(toFind.getValue());
    }

    private Child defaultChild() {
        return Child.builder()
            .party(ChildParty.builder()
                .partyId(randomUUID().toString())
                .build())
            .build();
    }

    private Respondent defaultRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .partyId(randomUUID().toString())
                .build())
            .build();
    }

}
