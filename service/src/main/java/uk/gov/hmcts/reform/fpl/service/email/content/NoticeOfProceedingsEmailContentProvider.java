package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;

import static uk.gov.hmcts.reform.fpl.enums.TabLabel.HEARINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsEmailContentProvider extends AbstractEmailContentProvider {

    public AllocatedJudgeTemplateForNoticeOfProceedings buildAllocatedJudgeNotification(CaseData caseData) {

        return AllocatedJudgeTemplateForNoticeOfProceedings.builder()

            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .respondentLastName(getFirstRespondentLastName(caseData))
            .hearingDate(getHearingBookingStartDate(caseData))
            .caseUrl(getCaseUrl(caseData.getId(), HEARINGS))
            .judgeTitle(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()
                .getJudgeOrMagistrateTitle())
            .judgeName(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeName())
            .build();
    }

    private String getHearingBookingStartDate(CaseData caseData) {
        return caseData.getFirstHearing()
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
