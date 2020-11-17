package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

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

    public static JudgeAndLegalAdvisor getSelectedJudge(JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                        Judge allocatedJudge) {
        if (judgeAndLegalAdvisor.isUsingAllocatedJudge()) {
            return migrateJudgeAndLegalAdvisor(judgeAndLegalAdvisor, allocatedJudge);
        } else {
            return judgeAndLegalAdvisor;
        }
    }

    public static JudgeAndLegalAdvisor getJudgeForTabView(JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                          Judge allocatedJudge) {
        JudgeAndLegalAdvisor judgeForTabView = getSelectedJudge(judgeAndLegalAdvisor, allocatedJudge);
        removeAllocatedJudgeProperties(judgeForTabView);
        return judgeForTabView;
    }

    public static String getHearingJudge(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (!judgeAndLegalAdvisor.isUsingAllocatedJudge()) {
            return formatJudgeTitleAndName(judgeAndLegalAdvisor);
        } else {
            return null;
        }
    }

    private static JudgeAndLegalAdvisor migrateJudgeAndLegalAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                                    Judge allocatedJudge) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(allocatedJudge.getJudgeTitle())
            .otherTitle(allocatedJudge.getOtherTitle())
            .judgeLastName(allocatedJudge.getJudgeLastName())
            .judgeFullName(allocatedJudge.getJudgeFullName())
            .judgeEmailAddress(allocatedJudge.getJudgeEmailAddress())
            .legalAdvisorName(judgeAndLegalAdvisor.getLegalAdvisorName())
            .allocatedJudgeLabel(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .useAllocatedJudge(judgeAndLegalAdvisor.getUseAllocatedJudge())
            .build();
    }

    public static void removeAllocatedJudgeProperties(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        judgeAndLegalAdvisor.setAllocatedJudgeLabel(null);
        judgeAndLegalAdvisor.setUseAllocatedJudge(null);
    }

    public static JudgeAndLegalAdvisor prepareJudgeFields(JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                                          Judge allocatedJudge) {
        if (allocatedJudge.hasEqualJudgeFields(judgeAndLegalAdvisor)) {
            return judgeAndLegalAdvisor.reset();
        } else {
            judgeAndLegalAdvisor.setUseAllocatedJudge("No");
            return judgeAndLegalAdvisor;
        }
    }

    public static String buildAllocatedJudgeLabel(Judge judge) {
        return String.format("Case assigned to: %s %s", judge.getJudgeOrMagistrateTitle(), judge.getJudgeName());
    }

    private static String mapJudgeOrAdvisor(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (isBlank(judgeAndLegalAdvisor.getJudgeLastName())
            && judgeAndLegalAdvisor.getJudgeTitle() != MAGISTRATES) {
            return "";
        }

        switch (judgeAndLegalAdvisor.getJudgeTitle()) {
            case MAGISTRATES:
                String magistrateFullName = judgeAndLegalAdvisor.getJudgeFullName();
                return isBlank(magistrateFullName) ? "Justice of the Peace" : magistrateFullName + " (JP)";
            case OTHER:
                return judgeAndLegalAdvisor.getOtherTitle() + " " + judgeAndLegalAdvisor.getJudgeLastName();
            default:
                return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }
}
