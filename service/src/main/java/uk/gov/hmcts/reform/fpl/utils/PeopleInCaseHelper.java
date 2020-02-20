package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;

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

    public static String getAllocatedJudgeTitle(CaseData data){
        if(data.getAllocatedJudge().getJudgeTitle() == OTHER)
        {
            return data.getAllocatedJudge().getOtherTitle();
        }

        return data.getAllocatedJudge().getJudgeTitle().toString();
    }

    public static String getAllocatedJudgeName(CaseData data){
        if(data.getAllocatedJudge().getJudgeTitle() == MAGISTRATES)
        {
            return data.getAllocatedJudge().getJudgeFullName();
        }

        return data.getAllocatedJudge().getJudgeLastName();
    }
}
