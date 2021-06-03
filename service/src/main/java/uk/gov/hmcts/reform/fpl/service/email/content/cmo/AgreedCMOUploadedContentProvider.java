package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CMOReadyToSealTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DRAFT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AgreedCMOUploadedContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public CMOReadyToSealTemplate buildTemplate(HearingBooking hearing, AbstractJudge judge, CaseData caseData) {
        return CMOReadyToSealTemplate.builder()
            .caseUrl(getCaseUrl(caseData.getId(), DRAFT_ORDERS))
            .judgeName(getJudgeName((judge)))
            .judgeTitle(getJudgeTitle((judge)))
            .lastName(helper.getSubjectLineLastName(caseData))
            .subjectLineWithHearingDate(subject(hearing, caseData.getAllRespondents(), caseData.getFamilyManCaseNumber()))
            .build();
    }

    private String subject(HearingBooking hearing, List<Element<Respondent>> respondents, String familyManCaseNumber) {
        String subject = buildSubjectLine(familyManCaseNumber, respondents);
        if (hearing == null) {
            throw new NoHearingBookingException();
        }
        return String.format("%s, %s", subject, uncapitalize(hearing.toLabel()));
    }
}
