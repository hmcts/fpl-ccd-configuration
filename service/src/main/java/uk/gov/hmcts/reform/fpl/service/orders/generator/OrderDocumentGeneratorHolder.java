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
    private final C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;

    private final C23EPOAdditionalDocumentsPopulator c23EPOAdditionalDocumentsPopulator;

    private Map<Order, DocmosisParameterGenerator> typeToGenerator;
    private Map<Order, OrderAdditionalDocumentsHolder> orderAdditionalDocumentsHolder;

    public Map<Order, DocmosisParameterGenerator> getTypeToGenerator() {
        if (typeToGenerator == null) {
            typeToGenerator = List.of(
                c32CareOrderDocumentParameterGenerator,
                c23EPODocumentParameterGenerator
            ).stream().collect(Collectors.toMap(
                DocmosisParameterGenerator::accept,
                Function.identity()
            ));
        }
        return typeToGenerator;
    }

    public Map<Order, OrderAdditionalDocumentsHolder> getOrderAdditionalDocuments() {
        if (orderAdditionalDocumentsHolder == null) {
            orderAdditionalDocumentsHolder = List.of(
                c23EPOAdditionalDocumentsPopulator
            ).stream().collect(Collectors.toMap(
                OrderAdditionalDocumentsHolder::accept,
                Function.identity()
            ));
        }
        return orderAdditionalDocumentsHolder;
    }

}
