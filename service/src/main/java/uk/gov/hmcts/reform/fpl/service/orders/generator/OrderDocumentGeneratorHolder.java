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

    // parameter generators
    private final C21BlankOrderDocumentParameterGenerator c21BlankOrderDocumentParameterGenerator;
    private final C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;
    private final C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;
    private final C33InterimCareOrderDocumentParameterGenerator c33InterimCareOrderDocumentParameterGenerator;
    private final C35aSupervisionOrderDocumentParameterGenerator c35aSupervisionOrderDocumentParameterGenerator;
    private final C35bISODocumentParameterGenerator c35bISODocumentParameterGenerator;
    private final C47AAppointmentOfAChildrensGuardianParameterGenerator c47AParameterGenerator;

    // additional document collectors
    private final C23EPOAdditionalDocumentsCollector c23EPOAdditionalDocumentsCollector;

    private Map<Order, DocmosisParameterGenerator> typeToGenerator;
    private Map<Order, AdditionalDocumentsCollector> typeToAdditionalDocsCollector;

    public Map<Order, DocmosisParameterGenerator> getTypeToGenerator() {
        if (typeToGenerator == null) {
            typeToGenerator = List.of(
                c21BlankOrderDocumentParameterGenerator,
                c23EPODocumentParameterGenerator,
                c32CareOrderDocumentParameterGenerator,
                c33InterimCareOrderDocumentParameterGenerator,
                c35aSupervisionOrderDocumentParameterGenerator,
                c35bISODocumentParameterGenerator,
                c47AParameterGenerator
            ).stream().collect(Collectors.toMap(DocmosisParameterGenerator::accept, Function.identity()));
        }
        return typeToGenerator;
    }

    public Map<Order, AdditionalDocumentsCollector> getTypeToAdditionalDocumentsCollector() {
        if (typeToAdditionalDocsCollector == null) {
            typeToAdditionalDocsCollector = List.of(
                c23EPOAdditionalDocumentsCollector
            ).stream().collect(Collectors.toMap(AdditionalDocumentsCollector::accept, Function.identity()));
        }
        return typeToAdditionalDocsCollector;
    }

}
