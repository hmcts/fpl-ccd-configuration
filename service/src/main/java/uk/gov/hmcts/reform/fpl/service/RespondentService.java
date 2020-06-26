package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

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

    public List<Element<Respondent>> setPersistRepresentativeFlag(List<Element<Respondent>> respondents) {
        return respondents.stream()
            .peek(element -> element.getValue().setPersistRepresentedBy(YES.getValue())).collect(Collectors.toList());
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }
}
