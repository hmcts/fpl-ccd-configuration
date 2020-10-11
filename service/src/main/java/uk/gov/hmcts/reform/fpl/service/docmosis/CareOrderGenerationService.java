package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    @Override
    DocmosisGeneratedOrder populateCustomOrderFields(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderSubtype subtype = orderTypeAndDocument.getSubtype();
        InterimEndDate interimEndDate = caseData.getInterimEndDate();

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            orderBuilder
                .orderTitle(getFullOrderType(orderTypeAndDocument))
                .childrenAct("Section 38 Children Act 1989")
                .exclusionClause(caseData.getExclusionClauseText());
        } else if (subtype == FINAL) {
            orderBuilder
                .orderTitle(getFullOrderType(orderTypeAndDocument.getType()))
                .childrenAct("Section 31 Children Act 1989");
        }

        int childrenCount = getChildrenCount(caseData);

        return orderBuilder
            .localAuthorityName(getLocalAuthorityName(caseData.getCaseLocalAuthority()))
            .orderDetails(getFormattedCareOrderDetails(childrenCount, caseData.getCaseLocalAuthority(),
                orderTypeAndDocument.isInterim(), interimEndDate))
            .build();
    }

    private String getFormattedCareOrderDetails(int numOfChildren,
                                                String caseLocalAuthority,
                                                boolean isInterim,
                                                InterimEndDate interimEndDate) {
        String childOrChildren = (numOfChildren == 1 ? "child is" : "children are");
        return String.format("It is ordered that the %s placed in the care of %s%s.",
            childOrChildren, getLocalAuthorityName(caseLocalAuthority),
            isInterim ? " until " + getInterimEndDateString(interimEndDate) : "");
    }

}
