package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
public class ChildrenService {

    public List<Element<Child>> prepareChildren(CaseData caseData) {
        List<Element<Child>> childCollection = new ArrayList<>();

        if (caseData.getAllChildren().isEmpty()) {
            Element<Child> expandedChildWithPartyId = element(Child.builder()
                .party(ChildParty.builder().partyId(randomUUID().toString()).build())
                .build());

            childCollection.add(expandedChildWithPartyId);

        } else if (caseData.getConfidentialChildren().isEmpty()) {
            return caseData.getAllChildren();

        } else {
            caseData.getAllChildren().forEach(element -> {
                if (element.getValue().containsConfidentialDetails()) {
                    Element<Child> confidentialChild = getElementToAdd(caseData.getConfidentialChildren(), element);
                    Child childWithConfidentialDetails = addConfidentialDetails(confidentialChild, element);

                    childCollection.add(element(element.getId(), childWithConfidentialDetails));
                } else {
                    childCollection.add(element);
                }
            });
        }
        return childCollection;
    }

    private Child addConfidentialDetails(Element<Child> confidentialChild, Element<Child> child) {
        return Child.builder()
            .party(child.getValue().getParty().toBuilder()
                .firstName(confidentialChild.getValue().getParty().firstName)
                .lastName(confidentialChild.getValue().getParty().lastName)
                .email(confidentialChild.getValue().getParty().email)
                .telephoneNumber(confidentialChild.getValue().getParty().telephoneNumber)
                .address(confidentialChild.getValue().getParty().address)
                .build())
            .build();
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

                return element(element.getId(), builder.build());
            })
            .collect(toList());
    }

    public List<Element<Child>> retainConfidentialDetails(List<Element<Child>> confidentialChildren) {
        List<Element<Child>> confidentialChildrenModified = new ArrayList<>();

        confidentialChildren.forEach(element -> confidentialChildrenModified.add(
            element(element.getId(), Child.builder().party(
                ChildParty.builder()
                    .firstName(element.getValue().getParty().firstName)
                    .lastName(element.getValue().getParty().lastName)
                    .address(element.getValue().getParty().address)
                    .telephoneNumber(element.getValue().getParty().telephoneNumber)
                    .email(element.getValue().getParty().email)
                    .build())
                .build())));

        return confidentialChildrenModified;
    }

    private void addHiddenValues(Element<Child> element, Child.ChildBuilder builder) {
        builder.party(element.getValue().getParty().toBuilder()
            .partyId(randomUUID().toString())
            .partyType(INDIVIDUAL)
            .build());
    }

    public String getChildrenLabel(List<Element<Child>> children) {
        if (isEmpty(children)) {
            return "No children in the case";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            final String name = children.get(i).getValue().getParty().getFullName();
            builder.append(String.format("Child %d: %s%n", i + 1, name));
        }

        return builder.toString();
    }

    public void addPageShowToCaseDetails(CaseDetails caseDetails, List<Element<Child>> children) {
        caseDetails.getData().put("pageShow", children.size() <= 1 ? "No" : "Yes");
    }
}
