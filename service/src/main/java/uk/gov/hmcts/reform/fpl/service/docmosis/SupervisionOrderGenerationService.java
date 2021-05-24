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
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupervisionOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    private static final String CHILDREN = "children";

    private final Time time;

    @Override
    DocmosisGeneratedOrder populateCustomOrderFields(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();
        GeneratedOrderSubtype subtype = orderTypeAndDocument.getSubtype();
        InterimEndDate interimEndDate = caseData.getInterimEndDate();
        int childrenCount = getChildrenCount(caseData);

        DocmosisGeneratedOrderBuilder<?, ?> orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            return orderBuilder
                .orderTitle(getFullOrderType(orderTypeAndDocument))
                .childrenAct("Section 38 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                .orderDetails(getFormattedInterimSupervisionOrderDetails(childrenCount,
                    caseData.getCaseLocalAuthority(), interimEndDate))
                .build();
        } else {
            return orderBuilder
                .orderTitle(getFullOrderType(orderTypeAndDocument.getType()))
                .childrenAct("Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989")
                .orderDetails(getFormattedFinalSupervisionOrderDetails(childrenCount,
                    caseData.getCaseLocalAuthority(), caseData.getOrderMonths()))
                .build();
        }
    }

    private String getFormattedInterimSupervisionOrderDetails(int numOfChildren, String caseLocalAuthority,
                                                              InterimEndDate interimEndDate) {
        return String.format("It is ordered that %s supervises the %s until %s.",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : CHILDREN,
            getInterimEndDateString(interimEndDate));
    }

    private String getFormattedFinalSupervisionOrderDetails(int numOfChildren,
                                                            String caseLocalAuthority,
                                                            int numOfMonths) {
        return String.format(
            "It is ordered that %s supervises the %s for %d months from the date of this order.",
            getLocalAuthorityName(caseLocalAuthority),
            (numOfChildren == 1) ? "child" : CHILDREN,
            numOfMonths);
    }

}

