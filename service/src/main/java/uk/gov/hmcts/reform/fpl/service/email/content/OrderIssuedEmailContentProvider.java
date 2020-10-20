package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;
    private final GeneratedOrderService generatedOrderService;
    private final Time time;

    public Map<String, Object> buildParametersWithoutCaseUrl(final CaseData caseData,
                                                             final byte[] documentContents,
                                                             final IssuedOrderType issuedOrderType) {
        return ImmutableMap.<String, Object>builder()
            .put("orderType", getTypeOfOrder(caseData, issuedOrderType))
            .put("callout", (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) ? buildCallout(caseData) : "")
            .put("courtName", config.getCourt(caseData.getCaseLocalAuthority()).getName())
            .putAll(linkToAttachedDocument(documentContents))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }

    public Map<String, Object> buildParametersWithCaseUrl(final CaseData caseData,
                                                          final byte[] documentContents,
                                                          final IssuedOrderType issuedOrderType) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildParametersWithoutCaseUrl(caseData, documentContents, issuedOrderType))
            .put("caseUrl", getCaseUrl(caseData.getId(), "OrdersTab"))
            .build();
    }

    public AllocatedJudgeTemplateForGeneratedOrder buildAllocatedJudgeOrderIssuedNotification(CaseData caseData) {

        JudgeAndLegalAdvisor judge = getAllocatedJudge(caseData);

        AllocatedJudgeTemplateForGeneratedOrder judgeTemplate = new AllocatedJudgeTemplateForGeneratedOrder();
        judgeTemplate.setOrderType(getTypeOfOrder(caseData, GENERATED_ORDER));
        judgeTemplate.setCallout(buildCallout(caseData));
        judgeTemplate.setCaseUrl(getCaseUrl(caseData.getId(), "OrdersTab"));
        judgeTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        judgeTemplate.setJudgeTitle(judge.getJudgeOrMagistrateTitle());
        judgeTemplate.setJudgeName(judge.getJudgeName());

        return judgeTemplate;
    }

    private JudgeAndLegalAdvisor getAllocatedJudge(CaseData caseData) {
        return generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);
    }

    private String buildCallout(final CaseData caseData) {
        HearingBooking hearing = null;
        if (caseData.hasFutureHearing(caseData.getHearingDetails())) {
            hearing = caseData.getMostUrgentHearingBookingAfter(time.now());
        }
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            hearing);
    }

    private String getTypeOfOrder(CaseData caseData, IssuedOrderType issuedOrderType) {
        String orderType;
        if (issuedOrderType == GENERATED_ORDER) {
            orderType = Iterables.getLast(caseData.getOrderCollection()).getValue().getType();
        } else {
            orderType = issuedOrderType.getLabel();
        }

        return orderType.toLowerCase();
    }

    private Map<String, Object> linkToAttachedDocument(final byte[] documentContents) {
        ImmutableMap.Builder<String, Object> url = ImmutableMap.builder();

        generateAttachedDocumentLink(documentContents).ifPresent(
            attachedDocumentLink -> url.put("documentLink", attachedDocumentLink));

        return url.build();
    }
}
