package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.TemporaryHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemporaryHearingJudgeContentProvider extends AbstractEmailContentProvider {

    public TemporaryHearingJudgeTemplate buildNotificationParameters(CaseData caseData, HearingBooking hearingBooking) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();

        TemporaryHearingJudgeTemplate temporaryHearingJudgeTemplate = TemporaryHearingJudgeTemplate.builder()
            .judgeTitle(getJudgeTitle(judgeAndLegalAdvisor))
            .judgeName(getJudgeName(judgeAndLegalAdvisor))
            .hearingType(hearingBooking.getType().getLabel())
            .caseUrl(getCaseUrl(caseData.getId()))
            .callout(buildCallout(caseData, hearingBooking))
            .build();

        setAllocatedJudgeFields(caseData.getAllocatedJudge(), temporaryHearingJudgeTemplate);

        return temporaryHearingJudgeTemplate;
    }

    private void setAllocatedJudgeFields(Judge allocatedJudge, TemporaryHearingJudgeTemplate template) {
        if (allocatedJudge != null) {
            template.setAllocatedJudgeName(allocatedJudge.getJudgeName());
            template.setAllocatedJudgeTitle(allocatedJudge.getJudgeOrMagistrateTitle());
            template.setHasAllocatedJudge(YES.getValue());
        } else {
            template.setHasAllocatedJudge(NO.getValue());
            template.setAllocatedJudgeName("");
            template.setAllocatedJudgeTitle("");
        }
    }

    private String buildCallout(final CaseData caseData, final HearingBooking hearingBooking) {
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearingBooking);
    }
}
