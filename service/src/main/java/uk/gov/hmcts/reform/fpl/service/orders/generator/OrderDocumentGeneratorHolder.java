package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderDocumentGeneratorHolder {

    private final C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;

    private Map<Order, SingleOrderDocumentParameterGenerator> typeToGenerator;

    public Map<Order, SingleOrderDocumentParameterGenerator> getTypeToGenerator() {
        if (typeToGenerator == null) {
            typeToGenerator = List.of(
                c32CareOrderDocumentParameterGenerator
            ).stream().collect(Collectors.toMap(
                SingleOrderDocumentParameterGenerator::accept,
                Function.identity()
            ));
        }
        return typeToGenerator;
    }


}
