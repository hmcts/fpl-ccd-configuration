package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

@Service
public class ChildrenService {

    public List<Element<Child>> prepareChildren(CaseData caseData) {
        List<Element<Child>> childCollection = new ArrayList<>();

        if (caseData.getAllChildren().isEmpty()) {
            childCollection.add(emptyElementWithPartyId());

        } else if (caseData.getConfidentialChildren().isEmpty()) {
            return caseData.getAllChildren();

        } else {
            caseData.getAllChildren().forEach(element -> {
                if (element.getValue().containsConfidentialDetails()) {
                    childCollection.add(getElementToAdd(caseData.getConfidentialChildren(), element));
                } else {
                    childCollection.add(element);
                }
            });
        }
        return childCollection;
    }

    // expands collection in UI. A value (in this case partyId) needs to be set to expand the collection.
    private Element<Child> emptyElementWithPartyId() {
        return ElementUtils.element(Child.builder()
            .party(ChildParty.builder().partyId(randomUUID().toString()).build())
            .build());
    }

    private Element<Child> getElementToAdd(List<Element<Child>> confidentialChildren, Element<Child> element) {
        return confidentialChildren.stream()
            .filter(confidentialChild -> confidentialChild.getId().equals(element.getId()))
            .findFirst()
            .orElse(element);
    }

    public List<Element<Child>> modifyHiddenValues(List<Element<Child>> children) {
        return children.stream()
            .map(element -> {
                Child.ChildBuilder builder = Child.builder();

                if (element.getValue().getParty().getPartyId() == null) {
                    addHiddenValues(element, builder);
                } else {
                    builder.party(element.getValue().getParty().toBuilder().build());
                }

                if (element.getValue().containsConfidentialDetails()) {
                    builder.party(element.getValue().getParty().toBuilder()
                        .address(null)
                        .telephoneNumber(null)
                        .email(null)
                        .build());
                }

                return Element.<Child>builder()
                    .id(element.getId())
                    .value(builder.build())
                    .build();
            })
            .collect(toList());
    }

    private void addHiddenValues(Element<Child> element, Child.ChildBuilder builder) {
        builder.party(element.getValue().getParty().toBuilder()
            .partyId(randomUUID().toString())
            .partyType(INDIVIDUAL)
            .build());
    }
}
