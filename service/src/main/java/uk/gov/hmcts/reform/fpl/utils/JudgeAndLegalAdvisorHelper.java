package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

public class JudgeAndLegalAdvisorHelper {

    private JudgeAndLegalAdvisorHelper() {
    }

    public static String getLegalAdvisorName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return Optional.ofNullable(judgeAndLegalAdvisor)
            .map(JudgeAndLegalAdvisor::getLegalAdvisorName)
            .orElse("");
    }

    public static String formatJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return Optional.ofNullable(judgeAndLegalAdvisor)
            .filter(judge -> judge.getJudgeTitle() != null)
            .map(JudgeAndLegalAdvisorHelper::mapJudgeOrAdvisor)
            .orElse("");
    }

    public static String formatJudgeTitleAndNameForDraftSDO(JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                            String emptyPlaceholder) {
        return Optional.ofNullable(judgeAndLegalAdvisor)
            .filter(judge -> judge.getJudgeTitle() != null)
            .map(JudgeAndLegalAdvisorHelper::mapJudgeOrAdvisor)
            .orElse(emptyPlaceholder);
    }

    private static String mapJudgeOrAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (isBlank(judgeAndLegalAdvisor.getJudgeLastName()) && isBlank(judgeAndLegalAdvisor.getJudgeFullName())) {
            return "";
        }

        if (judgeAndLegalAdvisor.getJudgeTitle() == MAGISTRATES) {
            return judgeAndLegalAdvisor.getJudgeFullName() + " (JP)";
        } else if (judgeAndLegalAdvisor.getJudgeTitle() == OTHER) {
            return judgeAndLegalAdvisor.getOtherTitle() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        } else {
            return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }
}
