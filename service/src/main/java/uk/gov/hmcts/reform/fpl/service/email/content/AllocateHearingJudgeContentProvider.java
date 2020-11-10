package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.AllocateHearingJudgeTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllocateHearingJudgeContentProvider extends AbstractEmailContentProvider {

    public AllocateHearingJudgeTemplate buildNotificationParameters(CaseData caseData, HearingBooking hearingBooking) {
        AllocateHearingJudgeTemplate allocatedJudgeTemplate = new AllocateHearingJudgeTemplate();
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();

        allocatedJudgeTemplate.setJudgeTitle(judgeAndLegalAdvisor.getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(judgeAndLegalAdvisor.getJudgeName());
        allocatedJudgeTemplate.setHearingType(hearingBooking.getType().getLabel());
        allocatedJudgeTemplate.setCaseUrl(getCaseUrl(caseData.getId()));
        allocatedJudgeTemplate.setCallout(buildCallout(caseData, hearingBooking));

        return allocatedJudgeTemplate;
    }

    private String buildCallout(final CaseData caseData, final HearingBooking hearingBooking) {
        return buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearingBooking);
    }
}
