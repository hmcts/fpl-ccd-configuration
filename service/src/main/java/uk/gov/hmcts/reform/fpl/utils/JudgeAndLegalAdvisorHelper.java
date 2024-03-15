package uk.gov.hmcts.reform.fpl.utils;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

@Component
public class JudgeAndLegalAdvisorHelper {

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
        if (isEmpty(judgeAndLegalAdvisor) || judgeAndLegalAdvisor.isUsingAllocatedJudge()) {
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
        JudgeAndLegalAdvisor.JudgeAndLegalAdvisorBuilder builder = JudgeAndLegalAdvisor.builder()
            .legalAdvisorName(isEmpty(judgeAndLegalAdvisor) ? null : judgeAndLegalAdvisor.getLegalAdvisorName())
            .allocatedJudgeLabel(isEmpty(judgeAndLegalAdvisor) ? null : judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .useAllocatedJudge(isEmpty(judgeAndLegalAdvisor) ? null : judgeAndLegalAdvisor.getUseAllocatedJudge());

        if (isNotEmpty(allocatedJudge)) {
            builder = builder.judgeTitle(allocatedJudge.getJudgeTitle())
                .otherTitle(allocatedJudge.getOtherTitle())
                .judgeLastName(allocatedJudge.getJudgeLastName())
                .judgeFullName(allocatedJudge.getJudgeFullName())
                .judgeEmailAddress(allocatedJudge.getJudgeEmailAddress());
        }

        return builder.build();
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

    public Optional<JudgeAndLegalAdvisor> buildForHearing(CaseData caseData,
                                                          Optional<Element<HearingBooking>> hearing) {
        Judge allocatedJudge = caseData.getAllocatedJudge();
        boolean allocatedJudgeExists = caseData.allocatedJudgeExists();

        if (hearing.isPresent()) {
            if (allocatedJudgeExists) {
                JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearing.get().getValue().getJudgeAndLegalAdvisor();
                return Optional.of(prepareJudgeFields(judgeAndLegalAdvisor, allocatedJudge)
                    .toBuilder()
                    .allocatedJudgeLabel(buildAllocatedJudgeLabel(allocatedJudge)).build());
            }

            return Optional.of(hearing.get().getValue().getJudgeAndLegalAdvisor());
        }

        if (allocatedJudgeExists) {
            return Optional.of(JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel(buildAllocatedJudgeLabel(allocatedJudge))
                .build());
        }

        return Optional.empty();
    }
}
