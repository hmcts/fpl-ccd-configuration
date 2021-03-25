package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
public class RespondentService {

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

    public List<Element<Respondent>> persistRepresentativesRelationship(List<Element<Respondent>> newRespondents,
                                                                        List<Element<Respondent>> oldRespondents) {
        oldRespondents.forEach(respondentElement -> {
            Optional<Element<Respondent>> respondentOptional = findElement(respondentElement.getId(), newRespondents);
            respondentOptional.ifPresent(respondent ->
                respondent.getValue().setRepresentedBy(respondentElement.getValue().getRepresentedBy())
            );
        });

        return newRespondents;
    }

    public List<Respondent> getRespondentsWithLegalRepresentation(List<Element<Respondent>> respondents) {
        return respondents
            .stream()
            .map(Element::getValue)
            .filter(respondent -> !isNull(respondent.getLegalRepresentation()) && respondent.getLegalRepresentation()
                .equals(YES.getValue()))
            .collect(Collectors.toList());
    }

    public List<String> getRespondentSolicitorEmails(List<Respondent> respondents) {
        return respondents
            .stream()
            .map(Respondent::getSolicitor)
            .map(RespondentSolicitor::getEmail)
            .filter(Objects::nonNull)
            .filter(email -> !email.isEmpty())
            .collect(Collectors.toList());
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }
}
