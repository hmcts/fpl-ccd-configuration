package uk.gov.hmcts.reform.fpl.service.docmosis;

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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_WITH_ORDINAL_SUFFIX;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

public abstract class GeneratedOrderTemplateDataGeneration
    extends DocmosisTemplateDataGeneration<DocmosisGeneratedOrder> {

    private final CaseDataExtractionService caseDataExtractionService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    public GeneratedOrderTemplateDataGeneration(
        CaseDataExtractionService caseDataExtractionService,
        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration) {
        this.caseDataExtractionService = caseDataExtractionService;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    abstract DocmosisGeneratedOrderBuilder<?, ?> populateCustomOrderFields(CaseData caseData);

    @Override
    public DocmosisGeneratedOrder getTemplateData(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderType orderType = orderTypeAndDocument.getType();

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = populateCustomOrderFields(caseData);

        OrderStatus orderStatus = caseData.getGeneratedOrderStatus();
        if (orderStatus == DRAFT) {
            orderBuilder.draftbackground(getDraftWaterMarkData());
        }

        if (orderStatus == SEALED) {
            orderBuilder.courtseal(getCourtSealData());
        }

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(caseData.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        DocmosisJudgeAndLegalAdvisor docmosisJudgeAndLegalAdvisor
            = caseDataExtractionService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor);

        return orderBuilder
            .orderType(orderType)
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(caseDataExtractionService.getCourtName(caseData.getCaseLocalAuthority()))
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .judgeAndLegalAdvisor(docmosisJudgeAndLegalAdvisor)
            .children(getChildrenDetails(caseData))
            .furtherDirections(caseData.getFurtherDirectionsText())
            .crest(getCrestData())
            .build();
    }

    int getChildrenCount(CaseData caseData) {
        return getChildrenDetails(caseData).size();
    }

    List<DocmosisChild> getChildrenDetails(CaseData caseData) {
        List<Element<Child>> selectedChildren = getSelectedChildren(caseData.getAllChildren(),
            caseData.getChildSelector(), caseData.getOrderAppliesToAllChildren());
        return caseDataExtractionService.getChildrenDetails(selectedChildren);
    }

    List<Element<Child>> getSelectedChildren(List<Element<Child>> allChildren, ChildSelector selector,
        String choice) {
        if (useAllChildren(choice)) {
            return allChildren;
        }

        return selector.getSelected().stream()
            .map(allChildren::get)
            .collect(Collectors.toList());
    }

    boolean useAllChildren(String choice) {
        // If there is only one child in the case then the choice will be null
        return choice == null || "Yes".equals(choice);
    }

    String getLocalAuthorityName(String caseLocalAuthority) {
        return localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);
    }

    String getInterimEndDateString(InterimEndDate interimEndDate) {
        return interimEndDate.toLocalDateTime()
            .map(dateTime -> {
                final String dayOrdinalSuffix = getDayOfMonthSuffix(dateTime.getDayOfMonth());
                return formatLocalDateTimeBaseUsingFormat(
                    dateTime, String.format(DATE_WITH_ORDINAL_SUFFIX, dayOrdinalSuffix));
            })
            .orElse("the end of the proceedings");
    }
}
