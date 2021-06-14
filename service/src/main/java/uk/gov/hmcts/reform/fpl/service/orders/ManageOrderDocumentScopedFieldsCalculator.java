package uk.gov.hmcts.reform.fpl.service.orders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ManageOrderDocumentScopedFieldsCalculator {

    public List<String> calculate() {
        List<String> fields = Stream.of(Order.values()).flatMap(order -> order.getQuestions().stream())
            .flatMap(questionBlock -> questionBlock.getDataFields().stream())
            .distinct()
            .collect(Collectors.toList());

        fields.addAll(List.of(
            "manageOrdersOperation",
            "manageOrdersType",
            "manageOrdersUploadType",
            "manageOrdersState",
            "orderTempQuestions",
            "hearingDetailsSectionSubHeader",
            "issuingDetailsSectionSubHeader",
            "childrenDetailsSectionSubHeader",
            "orderDetailsSectionSubHeader"
        ));

        return fields;
    }
}
