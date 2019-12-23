package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

@Service
public class ChildrenService {

    public List<Element<Child>> expandCollection(List<Element<Child>> children) {
        List<Element<Child>> populatedChildren = new ArrayList<>();

        if (children.isEmpty()) {
            populatedChildren.add(Element.<Child>builder()
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());

            return populatedChildren;
        } else {
            return children;
        }
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

                if (hiddenContactDetails(element)) {
                    builder.party(element.getValue().getParty().toBuilder()
                        .address(null)
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
            .partyId(UUID.randomUUID().toString())
            .partyType(INDIVIDUAL)
            .build());
    }

    private boolean hiddenContactDetails(Element<Child> element) {
        String contactDetails = element.getValue().getParty().getDetailsHidden();

        return contactDetails != null && contactDetails.equals("Yes");
    }
}
