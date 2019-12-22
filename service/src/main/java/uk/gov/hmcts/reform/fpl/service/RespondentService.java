package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class RespondentService {

    //TODO refactor to reduce complexity (code smell), or achieve in less complex way
    @SuppressWarnings("squid:S2583")
    public List<Element<Respondent>> expandRespondentCollection(CaseData caseData) {
        List<Element<Respondent>> populatedRespondents = new ArrayList<>();

        if (caseData.getRespondents1() == null) { // squid:S2583: value can be null in CCD JSON
            populatedRespondents.add(Element.<Respondent>builder()
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .partyId(UUID.randomUUID().toString())
                        .build())
                    .build())
                .build());
            return populatedRespondents;
        } else {
            for (Element<Respondent> respondent : caseData.getRespondents1()) {
                String contactDetails = respondent.getValue().getParty().getContactDetailsHidden();

                if (contactDetails != null && contactDetails.equals("Yes")) {
                    if (caseData.getConfidentialRespondents() != null) {
                        for (Element<Respondent> confidentialRespondent : caseData.getConfidentialRespondents()) {
                            if (isSameRespondentById(respondent, confidentialRespondent)) {
                                populatedRespondents.add(confidentialRespondent);
                                break;
                            }
                        }
                    }
                } else {
                    populatedRespondents.add(respondent);
                }
            }
            return populatedRespondents;
        }
    }

    public List<Element<Respondent>> modifyHiddenValues(CaseData caseData) {
        return caseData.getRespondents1().stream()
            .map(element -> {
                Respondent.RespondentBuilder respondentBuilder = Respondent.builder();

                if (element.getValue().getParty().getPartyId() == null) {
                    respondentBuilder.party(element.getValue().getParty().toBuilder()
                        .partyId(UUID.randomUUID().toString())
                        .partyType(PartyType.INDIVIDUAL)
                        .build());
                } else {
                    respondentBuilder.party(element.getValue().getParty().toBuilder().build());
                }

                String contactDetails = element.getValue().getParty().getContactDetailsHidden();
                if (contactDetails != null && contactDetails.equals("Yes")) {
                    respondentBuilder.party(element.getValue().getParty().toBuilder()
                        .address(null)
                        .telephoneNumber(null)
                        .build());
                }

                return Element.<Respondent>builder()
                    .id(element.getId())
                    .value(respondentBuilder.build())
                    .build();
            })
            .collect(toList());
    }

    public String buildRespondentLabel(List<Element<Respondent>> respondents) {
        StringBuilder sb = new StringBuilder();

        if (isNotEmpty(respondents)) {
            for (int i = 0; i < respondents.size(); i++) {
                RespondentParty respondentParty = respondents.get(i).getValue().getParty();

                sb.append(String.format("Respondent %d - %s", i + 1, getRespondentFullName(respondentParty)))
                    .append("\n");
            }
        } else {
            sb.append("No respondents on the case");
        }

        return sb.toString();
    }

    public List<Element<Respondent>> buildConfidentialRespondentsList(CaseData caseData) {
        List<Element<Respondent>> confidentialRespondents = new ArrayList<>();

        //TODO double check this: is there a nicer way?
        for (Element<Respondent> respondent : caseData.getRespondents1()
        ) {
            if (respondent.getValue() != null
                && respondent.getValue().getParty() != null
                && respondent.getValue().getParty().getContactDetailsHidden() != null
                && respondent.getValue().getParty().getContactDetailsHidden().equals("Yes")) {
                confidentialRespondents.add(respondent);
            }
        }
        return confidentialRespondents;
    }

    public boolean userInputtedRespondentExists(List<Element<Respondent>> respondents) {
        return (isNotEmpty(respondents) && !respondents.get(0).getValue().getParty().equals(RespondentParty.builder()
            .address(Address.builder().build())
            .telephoneNumber(Telephone.builder().build())
            .build()));
    }

    private boolean isSameRespondentById(Element<Respondent> respondent, Element<Respondent> confidentialRespondent) {
        return confidentialRespondent.getId().equals(respondent.getId());

    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }
}
