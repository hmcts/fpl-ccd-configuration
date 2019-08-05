package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

    @SuppressWarnings("unchecked")
    public CaseDetails addHiddenValues(CaseDetails caseDetails) {
        CaseDetails.CaseDetailsBuilder caseDetailsBuilder = caseDetails.toBuilder();

        if (caseDetails.getData().get("children1") != null) {
            List<Element<Child>> children1 = (List<Element<Child>>) caseDetails.getData().get("children1");

            List<Element<Child>> alteredChildren = children1.stream()
                .map(element -> {
                    Child.ChildBuilder childBuilder = Child.builder();

                    if (element.getValue().getParty().getPartyId() == null) {
                        childBuilder
                            .party(element.getValue().getParty().toBuilder()
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

            caseDetailsBuilder.data(ImmutableMap.of(
                "children1", alteredChildren
            ));
        }

        return caseDetailsBuilder.build();
    }
}
