package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.GeneratedOrderService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderIssuedEmailContentProvider extends AbstractEmailContentProvider {
    private final HmctsCourtLookupConfiguration config;
    private final EmailNotificationHelper emailNotificationHelper;
    private final ObjectMapper mapper;
    private final GeneratedOrderService generatedOrderService;

    public Map<String, Object> buildParametersWithoutCaseUrl(final CaseDetails caseDetails,
                                                             final String localAuthorityCode,
                                                             final byte[] documentContents,
                                                             final IssuedOrderType issuedOrderType) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .put("orderType", getTypeOfOrder(caseData, issuedOrderType))
            .put("callout", (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) ? buildCallout(caseData) : "")
            .put("courtName", config.getCourt(localAuthorityCode).getName())
            .putAll(linkToAttachedDocument(documentContents))
            .put("respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }

    public Map<String, Object> buildParametersWithCaseUrl(final CaseDetails caseDetails,
                                                          final String localAuthorityCode,
                                                          final byte[] documentContents,
                                                          final IssuedOrderType issuedOrderType) {
        return ImmutableMap.<String, Object>builder()
            .putAll(buildParametersWithoutCaseUrl(caseDetails, localAuthorityCode, documentContents,
                issuedOrderType))
            .put("caseUrl", getCaseUrl(caseDetails.getId()))
            .build();
    }

    public AllocatedJudgeTemplateForGeneratedOrder buildAllocatedJudgeOrderIssuedNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        JudgeAndLegalAdvisor judge = getAllocatedJudge(caseData);

        AllocatedJudgeTemplateForGeneratedOrder judgeTemplate = new AllocatedJudgeTemplateForGeneratedOrder();
        judgeTemplate.setOrderType(getTypeOfOrder(caseData, GENERATED_ORDER));
        judgeTemplate.setCallout(buildCallout(caseData));
        judgeTemplate.setCaseUrl(getCaseUrl(caseDetails.getId()));
        judgeTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        judgeTemplate.setJudgeTitle(judge.getJudgeOrMagistrateTitle());
        judgeTemplate.setJudgeName(judge.getJudgeName());

        return judgeTemplate;
    }

    private JudgeAndLegalAdvisor getAllocatedJudge(CaseData caseData) {
        return generatedOrderService.getAllocatedJudgeFromMostRecentOrder(caseData);
    }

    private String buildCallout(CaseData caseData) {
        return "^" + emailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix(
            caseData,
            caseData.getHearingDetails());
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
