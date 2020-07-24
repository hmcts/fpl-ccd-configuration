package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NewCMOUploadedContentProvider extends AbstractEmailContentProvider {

    private final EmailNotificationHelper emailHelper;

    public CMOReadyToSealTemplate buildTemplate(HearingBooking hearing, CaseData caseData, Long caseId,
                                                AbstractJudge judge) {
        return new CMOReadyToSealTemplate()
            .setCaseUrl(getCaseUrl(caseId))
            .setJudgeName(judge.getJudgeName())
            .setJudgeTitle(judge.getJudgeOrMagistrateTitle())
            .setRespondentLastName(getFirstRespondentLastName(caseData.getAllRespondents()))
            .setSubjectLineWithHearingDate(buildSubjectLine(caseData, hearing));
    }

    private String buildSubjectLine(CaseData caseData, HearingBooking hearing) {
        return String.format("%s, %s", emailHelper.buildSubjectLine(caseData), hearing.toLabel(DATE));
    }
}
