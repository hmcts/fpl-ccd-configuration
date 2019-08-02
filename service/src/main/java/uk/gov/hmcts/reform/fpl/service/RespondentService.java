package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class RespondentService {

    public String setMigratedValue(CaseData caseData) {
        if (caseData.getRespondents1() != null || caseData.getRespondents() == null) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public List<Element<MigratedRespondent>> expandRespondentCollection(CaseData caseData) {
        if (caseData.getRespondents1() == null) {
            List<Element<MigratedRespondent>> populatedRespondent = new ArrayList<>();

            populatedRespondent.add(Element.<MigratedRespondent>builder()
                .value(MigratedRespondent.builder()
                    .party(RespondentParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());

            return populatedRespondent;
        } else {
            return caseData.getRespondents1();
        }
    }

    public List<Element<MigratedRespondent>> addHiddenValues(CaseData caseData) {
        List<Element<MigratedRespondent>> respondents = new ArrayList<>();

        if (caseData.getRespondents1() != null) {
            respondents = caseData.getRespondents1().stream()
                .map(element -> {
                    MigratedRespondent.MigratedRespondentBuilder respondentBuilder = MigratedRespondent.builder();

                    if (element.getValue().getParty().getPartyId() == null) {
                        respondentBuilder
                            .party(element.getValue().getParty().toBuilder()
                                .partyId(UUID.randomUUID().toString())
                                .partyType(PartyType.INDIVIDUAL)
                                .build())
                            .leadRespondentIndicator("No");

                    } else {
                        respondentBuilder.party(element.getValue().getParty().toBuilder().build());
                    }

                    return Element.<MigratedRespondent>builder()
                        .id(element.getId())
                        .value(respondentBuilder.build())
                        .build();
                })
                .collect(toList());
        }
        return respondents;
    }
}
