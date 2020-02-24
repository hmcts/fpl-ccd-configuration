package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

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

    //To be deleted when print and post is available to use as part of FPLA-1287
    public static List<String> formatRepresentativesForPostNotification(List<Representative> representatives) {
        return representatives.stream()
            .map(representative -> String.format("%s\n%s", representative.getFullName(),
                representative.getAddress().getAddressAsString(", ")))
            .collect(toList());
    }

    public static String getAllocatedJudgeTitle(Judge judge) {
        if (judge.getJudgeTitle() == OTHER) {
            return judge.getOtherTitle();
        }
        return judge.getJudgeTitle().getLabel();
    }

    public static String getAllocatedJudgeName(Judge judge) {
        if (judge.getJudgeTitle() == MAGISTRATES) {
            return judge.getJudgeFullName();
        }
        return judge.getJudgeLastName();
    }
}
