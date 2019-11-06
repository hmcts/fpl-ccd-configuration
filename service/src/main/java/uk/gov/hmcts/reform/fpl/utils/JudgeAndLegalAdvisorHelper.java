package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

public class JudgeAndLegalAdvisorHelper {
    public static String getLegalAdvisorName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null) {
            return "";
        }

        return defaultIfNull(judgeAndLegalAdvisor.getLegalAdvisorName(), "");
    }

    public static String formatJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null || judgeAndLegalAdvisor.getJudgeTitle() == null) {
            return "";
        }

        if (judgeAndLegalAdvisor.getJudgeTitle() == MAGISTRATES) {
            return judgeAndLegalAdvisor.getJudgeFullName() + " (JP)";
        } else {
            return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }
}
