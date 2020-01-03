package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

@Service
public class RespondentService {

    public List<Element<Respondent>> prepareRespondents(CaseData caseData) {
        List<Element<Respondent>> respondentsCollection = new ArrayList<>();

        if (caseData.getAllRespondents().isEmpty()) {
            respondentsCollection.add(emptyElementWithPartyId());

        } else if (caseData.getConfidentialRespondents().isEmpty()) {
            return caseData.getAllRespondents();

        } else {
            caseData.getAllRespondents().forEach(element -> {
                if (element.getValue().containsConfidentialDetails()) {
                    respondentsCollection.add(getElementToAdd(caseData.getConfidentialRespondents(), element));
                } else {
                    respondentsCollection.add(element);
                }
            });
        }
        return respondentsCollection;
    }

    // expands collection in UI. A value (in this case partyId) needs to be set to expand the collection.
    private Element<Respondent> emptyElementWithPartyId() {
        return ElementUtils.element(Respondent.builder()
            .party(RespondentParty.builder().partyId(randomUUID().toString()).build())
            .build());
    }

    private Element<Respondent> getElementToAdd(List<Element<Respondent>> confidentialChildren,
                                                Element<Respondent> element) {
        return confidentialChildren.stream()
            .filter(confidentialChild -> confidentialChild.getId().equals(element.getId()))
            .findFirst()
            .orElse(element);
    }

    public List<Element<Respondent>> modifyHiddenValues(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .map(element -> {
                Respondent.RespondentBuilder builder = Respondent.builder();

                if (element.getValue().getParty().getPartyId() == null) {
                    addHiddenValues(element, builder);
                } else {
                    builder.party(element.getValue().getParty().toBuilder().build());
                }

                if (hiddenContactDetails(element)) {
                    builder.party(element.getValue().getParty().toBuilder()
                        .address(null)
                        .telephoneNumber(null)
                        .email(null)
                        .build());
                }

                return Element.<Respondent>builder()
                    .id(element.getId())
                    .value(builder.build())
                    .build();
            })
            .collect(toList());
    }

    private void addHiddenValues(Element<Respondent> element, Respondent.RespondentBuilder builder) {
        builder.party(element.getValue().getParty().toBuilder()
            .partyId(randomUUID().toString())
            .partyType(INDIVIDUAL)
            .build());
    }

    private boolean hiddenContactDetails(Element<Respondent> element) {
        String contactDetails = element.getValue().getParty().getContactDetailsHidden();

        return contactDetails != null && contactDetails.equals("Yes");
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

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }
}
