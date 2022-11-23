package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AbstractJudge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersRemovedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersRemovedContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public DraftOrdersRemovedTemplate buildContent(CaseData caseData, Optional<HearingBooking> hearing,
                                                   AbstractJudge judge, HearingOrder orders,
                                                   String removalReason) {

        return DraftOrdersRemovedTemplate.builder()
            .caseUrl(getCaseUrl(caseData.getId(), TabUrlAnchor.DRAFT_ORDERS))
            .judgeTitle(getJudgeTitle(judge))
            .judgeName(getJudgeName(judge))
            .respondentLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .subjectLineWithHearingDate(subject(
                hearing, caseData.getAllRespondents(), caseData.getFamilyManCaseNumber()
            ))
            .draftOrdersRemoved(orders.getTitle())
            .removalReason(removalReason)
            .build();
    }

    private String subject(Optional<HearingBooking> hearing, List<Element<Respondent>> respondents,
                           String familyManCaseNumber) {
        String subject = buildSubjectLine(familyManCaseNumber, respondents);
        if (hearing.isPresent()) {
            return String.format("%s, %s", subject, uncapitalize(hearing.get().toLabel()));
        } else {
            return subject;
        }
    }
}
