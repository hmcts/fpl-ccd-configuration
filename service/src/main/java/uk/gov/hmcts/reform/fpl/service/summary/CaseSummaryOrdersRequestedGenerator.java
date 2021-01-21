package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
public class CaseSummaryOrdersRequestedGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryOrdersRequested(concatOrdersRequested(caseData.getOrders()))
            .build();
    }

    private String concatOrdersRequested(Orders requestedOrders) {
        if (isNull(requestedOrders)
            || isNull(requestedOrders.getOrderType())
            || requestedOrders.getOrderType().isEmpty()) {

            return null;
        }

        return requestedOrders.getOrderType().stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
    }

}
