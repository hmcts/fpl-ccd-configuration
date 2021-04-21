package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

public abstract class GeneratedOrderTemplateDataGeneration
    extends DocmosisTemplateDataGeneration<DocmosisGeneratedOrder> {

    @Autowired
    private CaseDataExtractionService caseDataExtractionService;

    @Autowired
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    private ChildrenService childrenService;

    @Autowired
    private CaseDetailsHelper caseDetailsHelper;

    abstract DocmosisGeneratedOrder populateCustomOrderFields(CaseData caseData);

    @Override
    public DocmosisGeneratedOrder getTemplateData(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderType orderType = orderTypeAndDocument.getType();
        String dateOfIssue;
        if (EMERGENCY_PROTECTION_ORDER.equals(orderType)) {
            dateOfIssue = formatLocalDateTimeBaseUsingFormat(caseData.getDateAndTimeOfIssue(), DATE_TIME);
        } else {
            dateOfIssue = formatLocalDateToString(caseData.getDateOfIssue(), DATE);
        }

        var docmosisGeneratedOrderBuilder = populateCustomOrderFields(caseData).toBuilder();

        OrderStatus orderStatus = caseData.getGeneratedOrderStatus();
        if (orderStatus == DRAFT) {
            docmosisGeneratedOrderBuilder.draftbackground(getDraftWaterMarkData());
        }

        if (orderStatus == SEALED) {
            docmosisGeneratedOrderBuilder.courtseal(getCourtSealData());
        }

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(caseData.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        DocmosisJudgeAndLegalAdvisor docmosisJudgeAndLegalAdvisor
            = caseDataExtractionService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor);

        return docmosisGeneratedOrderBuilder
            .orderType(orderType)
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .ccdCaseNumber(caseDetailsHelper.formatCCDCaseNumber(caseData.getId()))
            .courtName(caseDataExtractionService.getCourtName(caseData.getCaseLocalAuthority()))
            .dateOfIssue(dateOfIssue)
            .judgeAndLegalAdvisor(docmosisJudgeAndLegalAdvisor)
            .children(getChildrenDetails(caseData))
            .furtherDirections(caseData.getFurtherDirectionsText())
            .crest(getCrestData())
            .build();
    }

    int getChildrenCount(CaseData caseData) {
        return getChildrenDetails(caseData).size();
    }

    private List<DocmosisChild> getChildrenDetails(CaseData caseData) {
        List<Element<Child>> selectedChildren = getSelectedChildren(caseData);
        return caseDataExtractionService.getChildrenDetails(selectedChildren);
    }

    List<Element<Child>> getSelectedChildren(CaseData caseData) {
        return childrenService.getSelectedChildren(caseData);
    }

    String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    String getInterimEndDateString(InterimEndDate interimEndDate) {
        return Optional.ofNullable(interimEndDate.getEndDateTime())
            .or(interimEndDate::toLocalDateTime)
            .map(dateTime -> {
                final String dayOrdinalSuffix = getDayOfMonthSuffix(dateTime.getDayOfMonth());
                return formatLocalDateTimeBaseUsingFormat(dateTime, String.format(DATE_WITH_ORDINAL_SUFFIX,
                    dayOrdinalSuffix));
            })
            .orElse("the end of the proceedings, or until a further order is made");
    }
}
