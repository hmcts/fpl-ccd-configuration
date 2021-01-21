package uk.gov.hmcts.reform.fpl.service.summary;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CaseSummaryOrdersRequestedGenerator implements CaseSummaryFieldsGenerator {

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryOrdersRequested(concatOrdersRequested(caseData.getOrders().getOrderType()))
            .build();
    }

    private String concatOrdersRequested(List<OrderType> requestedOrders) {
        return requestedOrders.stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
    }

}
