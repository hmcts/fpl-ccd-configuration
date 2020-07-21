package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CMOReadyToSealContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper emailHelper;

    public CMOReadyToSealTemplate buildTemplate(HearingBooking hearing, CaseData caseData, Long caseId) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearing.getJudgeAndLegalAdvisor();
        return new CMOReadyToSealTemplate()
            .setCaseUrl(getCaseUrl(caseId))
            .setJudgeName(judgeAndLegalAdvisor.getJudgeName())
            .setJudgeTitle(judgeAndLegalAdvisor.getJudgeOrMagistrateTitle())
            .setRespondentLastName(getFirstRespondentLastName(caseData.getAllRespondents()))
            .setSubjectLineWithHearingDate(buildSubjectLine(caseData, hearing));
    }

    private String buildSubjectLine(CaseData caseData, HearingBooking hearing) {
        return String.format("%s hearing %s",
            emailHelper.buildSubjectLine(caseData),
            formatLocalDateToString(hearing.getStartDate().toLocalDate(), DATE));
    }
}
