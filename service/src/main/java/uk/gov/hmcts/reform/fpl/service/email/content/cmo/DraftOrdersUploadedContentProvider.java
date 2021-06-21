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
import uk.gov.hmcts.reform.fpl.model.notify.cmo.DraftOrdersUploadedTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftOrdersUploadedContentProvider extends AbstractEmailContentProvider {
    private final EmailNotificationHelper helper;

    public DraftOrdersUploadedTemplate buildContent(CaseData caseData, HearingBooking hearing,
                                                    AbstractJudge judge, List<HearingOrder> orders) {

        return DraftOrdersUploadedTemplate.builder()
            .caseUrl(getCaseUrl(caseData.getId(), TabUrlAnchor.DRAFT_ORDERS))
            .judgeTitle(getJudgeTitle(judge))
            .judgeName(getJudgeName(judge))
            .lastName(helper.getSubjectLineLastName(caseData))
            .subjectLineWithHearingDate(subject(
                hearing, caseData.getAllRespondents(), caseData.getFamilyManCaseNumber()
            ))
            .draftOrders(formatOrders(orders))
            .build();
    }

    private String subject(HearingBooking hearing, List<Element<Respondent>> respondents, String familyManCaseNumber) {
        String subject = buildSubjectLine(familyManCaseNumber, respondents);
        if (hearing == null) {
            return subject;
        }
        return String.format("%s, %s", subject, uncapitalize(hearing.toLabel()));
    }

    private String formatOrders(List<HearingOrder> orders) {
        return orders.stream()
            .map(HearingOrder::getTitle)
            .collect(joining(lineSeparator()));
    }
}
