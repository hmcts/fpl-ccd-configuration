package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ChildrenMigrationService {

    @Autowired
    private ObjectMapper mapper;

    public String setMigratedValue(CaseData caseData) {
        if (caseData.getChildren1() != null || caseData.getChildren() == null) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public List<Element<Child>> expandChildrenCollection(CaseData caseData) {
        if (caseData.getChildren1() == null) {
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
                ChildParty.ChildPartyBuilder childPartyBuilder = element.getValue().getParty().toBuilder();

                if (element.getValue().getParty().getPartyId() == null) {
                    childPartyBuilder.partyId(UUID.randomUUID().toString());
                    childPartyBuilder.partyType(PartyType.INDIVIDUAL);
                }

                return Element.<Child>builder()
                    .id(element.getId())
                    .value(element.getValue().toBuilder().party(childPartyBuilder.build()).build())
                    .build();
            })
            .collect(toList());
    }
}
