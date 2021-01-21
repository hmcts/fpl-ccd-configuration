package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class AgreedCMOUploadedContentProvider extends AbstractEmailContentProvider {

    public CMOReadyToSealTemplate buildTemplate(HearingBooking hearing, Long caseId,
                                                AbstractJudge judge, List<Element<Respondent>> respondents,
                                                String familyManCaseNumber) {
        String judgeTitle = judge.getJudgeOrMagistrateTitle();
        String judgeName = judge.getJudgeName();

        if (MAGISTRATES.equals(judge.getJudgeTitle())) {
            judgeTitle = getMagistrateJudgeTitle(judge);
            judgeName = getMagistrateJudgeName(judge);
        }

        return CMOReadyToSealTemplate.builder()
            .caseUrl(getCaseUrl(caseId, DRAFT_ORDERS))
            .judgeName(judgeName)
            .judgeTitle(judgeTitle)
            .respondentLastName(getFirstRespondentLastName(respondents))
            .subjectLineWithHearingDate(subjectLine(hearing, respondents, familyManCaseNumber))
            .build();
    }

    private String subjectLine(HearingBooking hearing, List<Element<Respondent>> respondents,
                               String familyManCaseNumber) {
        return String.format("%s, %s", buildSubjectLine(familyManCaseNumber, respondents),
            uncapitalize(hearing.toLabel()));
    }

    private String getMagistrateJudgeName(AbstractJudge judge) {
        if (hasJudgeName(judge)) {
            return String.format("%s (JP)", judge.getJudgeName());
        }
        return "";
    }

    private String getMagistrateJudgeTitle(AbstractJudge judge) {
        if (hasJudgeName(judge)) {
            return "";
        }
        return "Justice of the Peace";
    }

    private boolean hasJudgeName(AbstractJudge judge) {
        return judge.getJudgeName() != null;
    }
}
