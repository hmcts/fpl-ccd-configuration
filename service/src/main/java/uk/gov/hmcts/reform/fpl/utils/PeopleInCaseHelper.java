package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PeopleInCaseHelper {

    private PeopleInCaseHelper() {
        // NO-OP
    }

    public static String getFirstRespondentLastName(List<Element<Respondent>> respondents) {
        return ElementUtils.unwrapElements(respondents).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(Respondent::getParty)
            .map(RespondentParty::getLastName)
            .orElse("");
    }

    public static List<String> formatRepresentativesForPostNotification(List<Representative> representatives) {
        return representatives.stream()
            .map(rep -> rep.getFullName() + "\n" + rep.getAddress().getAddressAsString())
            .collect(Collectors.toList());
    }
}
