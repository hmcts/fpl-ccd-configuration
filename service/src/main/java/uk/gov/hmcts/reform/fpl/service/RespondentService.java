package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

    @Autowired
    private ObjectMapper mapper;

    public String setMigratedValue(CaseData caseData) {
        if (caseData.getRespondents1() != null || caseData.getRespondents() == null) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public List<Element<MigratedRespondent>> expandRespondentCollection(CaseData caseData) {
        if (caseData.getRespondents() == null) {
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

    @SuppressWarnings("unchecked")
    public CaseData addHiddenValues(CaseDetails caseDetails) {
        CaseData data = mapper.convertValue(caseDetails.getData(), CaseData.class);
        CaseData.CaseDataBuilder caseDataBuilder = CaseData.builder();

        if (data.getRespondents1() != null) {
            List<Element<MigratedRespondent>> respondentParties = data.getRespondents1();

            List<RespondentParty> respondentPartyList = respondentParties.stream()
                .map(Element::getValue)
                .map(MigratedRespondent::getParty)
                .map(respondent -> {
                    RespondentParty.RespondentPartyBuilder partyBuilder = respondent.toBuilder();

                    if (respondent.getPartyId() == null) {
                        partyBuilder.partyId(UUID.randomUUID().toString());
                        partyBuilder.partyType(PartyType.INDIVIDUAL);
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Element<MigratedRespondent>> respondents = respondentPartyList.stream()
                .map(item -> Element.<MigratedRespondent>builder()
                    .id(UUID.randomUUID())
                    .value(MigratedRespondent.builder()
                        .party(item)
                        .leadRespondentIndicator("No")
                        .build())
                    .build())
                .collect(toList());

            caseDataBuilder.respondents1(respondents);
        }

        return caseDataBuilder.build();
    }
}
