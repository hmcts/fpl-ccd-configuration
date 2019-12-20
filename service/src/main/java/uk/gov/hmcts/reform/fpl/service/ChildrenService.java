package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

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
                String contactDetails = child.getValue().getParty().getDetailsHidden();

                if (contactDetails != null && contactDetails.equals("Yes")) {
                    if (caseData.getConfidentialChildren() != null) {
                        for (Element<Child> confidentialChild : caseData.getConfidentialChildren()) {
                            if (isSameChildById(child, confidentialChild)) {
                                populatedChildren.add(confidentialChild);
                                break;
                            }
                        }
                    }
                } else {
                    populatedChildren.add(child);
                }
            }
            return populatedChildren;
        }
    }

    public List<Element<Child>> modifyHiddenValues(CaseData caseData) {
        return caseData.getChildren1().stream()
            .map(element -> {
                Child.ChildBuilder childBuilder = Child.builder();

                if (element.getValue().getParty().getPartyId() == null) {
                    childBuilder.party(element.getValue().getParty().toBuilder()
                        .partyId(UUID.randomUUID().toString())
                        .partyType(PartyType.INDIVIDUAL)
                        .build());
                } else {
                    childBuilder.party(element.getValue().getParty().toBuilder().build());
                }

                String contactDetails = element.getValue().getParty().getDetailsHidden();
                if (contactDetails != null && contactDetails.equals("Yes")) {
                    childBuilder.party(element.getValue().getParty().toBuilder()
                        .address(null)
                        .build());
                }

                return Element.<Child>builder()
                    .id(element.getId())
                    .value(childBuilder.build())
                    .build();
            })
            .collect(toList());
    }

    public List<Element<Child>> buildConfidentialChildrenList(CaseData caseData) {
        List<Element<Child>> confidentialChildren = new ArrayList<>();

        //most likely there's a nicer way of doing this
        for (Element<Child> child : caseData.getChildren1()
        ) {
            if (child.getValue() != null
                && child.getValue().getParty() != null
                && child.getValue().getParty().getDetailsHidden() != null
                && child.getValue().getParty().getDetailsHidden().equals("Yes")) {
                confidentialChildren.add(child);
            }
        }
        return confidentialChildren;
    }

    public boolean expandedCollectionNotEmpty(List<Element<Child>> children) {
        return (isNotEmpty(children) && !children.get(0).getValue().getParty().equals(ChildParty.builder()
            .socialWorkerTelephoneNumber(Telephone.builder().build())
            .build()));
    }

    private boolean isSameChildById(Element<Child> child, Element<Child> confidentialChild) {
        return confidentialChild.getId().equals(child.getId());
    }

}
