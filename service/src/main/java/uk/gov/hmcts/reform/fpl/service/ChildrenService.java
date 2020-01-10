package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
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
        if (caseData.getChildren1() == null) { // squid:S2583: value can be null in CCD JSON
            List<Element<Child>> populatedChildren = new ArrayList<>();

            populatedChildren.add(Element.<Child>builder()
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());
            return populatedChildren;
        } else {
            return caseData.getChildren1();
        }
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
}
