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
        if (caseData.getChildren() == null) {
            List<Element<Child>> populatedRespondent = new ArrayList<>();

            populatedRespondent.add(Element.<Child>builder()
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());

            return populatedRespondent;
        } else {
            return caseData.getChildren1();
        }
    }

    @SuppressWarnings("unchecked")
    public CaseData addHiddenValues(CaseData caseData) {
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();

        if (caseData.getChildren1() != null) {
            List<Element<Child>> childrenParties = caseData.getChildren1();

            List<ChildParty> childrenPartyList = childrenParties.stream()
                .map(Element::getValue)
                .map(Child::getParty)
                .map(child -> {
                    ChildParty.ChildPartyBuilder partyBuilder = child.toBuilder();

                    if (child.getPartyId() == null) {
                        partyBuilder.partyId(UUID.randomUUID().toString());
                        partyBuilder.partyType(PartyType.INDIVIDUAL);
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Element<Child>> children = childrenPartyList.stream()
                .map(item -> Element.<Child>builder()
                    .id(UUID.randomUUID())
                    .value(Child.builder()
                        .party(item)
                        .build())
                    .build())
                .collect(toList());

            caseDataBuilder.children1(children);
        }

        return caseDataBuilder.build();
    }
}
