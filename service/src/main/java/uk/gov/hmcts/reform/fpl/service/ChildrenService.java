package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class ChildrenService {

    @SuppressWarnings("squid:S2583")
    public List<Element<Child>> expandChildrenCollection(CaseData caseData) {
        List<Element<Child>> populatedChildren = new ArrayList<>();
        if (caseData.getChildren1() == null) { // squid:S2583: value can be null in CCD JSON

            populatedChildren.add(Element.<Child>builder()
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());
            return populatedChildren;
        } else {
            for (Element<Child> child : caseData.getChildren1()) {
                if (child.getValue().getParty().getDetailsHidden().equals("Yes")) {
                    for (Element<Child> confidentialChild : caseData.getConfidentialChildren()) {
                        if (isSameChildById(child, confidentialChild)) {
                            populatedChildren.add(confidentialChild);
                            break;
                        }
                    }
                } else {
                    populatedChildren.add(child);
                }
            }
            return populatedChildren;
        }
    }

    private boolean isSameChildById(Element<Child> child, Element<Child> confidentialChild) {
        return confidentialChild.getId().equals(child.getId());
    }

    public List<Element<Child>> addHiddenValues(CaseData caseData) {
        return caseData.getChildren1().stream()
            .map(element -> {
                Child.ChildBuilder childBuilder = Child.builder();

                if (element.getValue().getParty().getPartyId() == null) {
                    childBuilder.party(element.getValue().getParty().toBuilder()
                        .partyId(UUID.randomUUID().toString())
                        .partyType(PartyType.INDIVIDUAL)
                        .build());
                    if (element.getValue().getParty().getDetailsHidden().equals("Yes")) {
                        childBuilder.party(element.getValue().getParty().toBuilder()
                            .address(Address.builder().build())
                            .build());
                    }
                } else {
                    childBuilder.party(element.getValue().getParty().toBuilder().build());
                }

                return Element.<Child>builder()
                    .id(element.getId())
                    .value(childBuilder.build())
                    .build();
            })
            .collect(toList());
    }

    public List<Element<Child>> addConfidentialChildren(CaseData caseData) {
        List<Element<Child>> confidentialChildren = new ArrayList<>();
        for (Element<Child> child : caseData.getChildren1()
        ) {
            if (child.getValue().getParty().getDetailsHidden().equals("Yes")) {
                confidentialChildren.add(child);
            }
        }
        return confidentialChildren;
    }
}
