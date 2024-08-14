package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderDocumentGeneratorHolder {

    // parameter generators
    private final A81PlacementBlankOrderDocumentParameterGenerator a81PlacementBlankOrderDocumentParameterGenerator;
    private final A70PlacementOrderDocumentParameterGenerator a70PlacementOrderDocumentParameterGenerator;
    private final C21BlankOrderDocumentParameterGenerator c21BlankOrderDocumentParameterGenerator;
    private final C23EPODocumentParameterGenerator c23EPODocumentParameterGenerator;
    private final C26SecureAccommodationOrderDocumentParameterGenerator
        c26SecureAccommodationOrderDocumentParameterGenerator;
    private final C34BAuthorityToRefuseContactOrderParameterGenerator
        c34BAuthorityToRefuseContactOrderParameterGenerator;
    private final C34aContactWithAChildInCareOrderDocumentParameterGenerator
        c34aContactWithAChildInCareOrderDocumentParameterGenerator;
    private final C29RecoveryOfAChildDocumentParameterGenerator c29RecoveryOfAChildDocumentParameterGenerator;
    private final C32CareOrderDocumentParameterGenerator c32CareOrderDocumentParameterGenerator;
    private final C32bDischargeOfCareOrderDocumentParameterGenerator c32bDischargeOfCareOrderDocumentParameterGenerator;
    private final C33InterimCareOrderDocumentParameterGenerator c33InterimCareOrderDocumentParameterGenerator;
    private final C35aSupervisionOrderDocumentParameterGenerator c35aSupervisionOrderDocumentParameterGenerator;
    private final C35bISODocumentParameterGenerator c35bISODocumentParameterGenerator;
    private final C39ChildAssessmentOrderParameterGenerator c39ChildAssessmentOrderParameterGenerator;
    private final C37EducationSupervisionOrderParameterGenerator c37EducationSupervisionOrderParameterGenerator;
    private final C43ChildArrangementOrderDocumentParameterGenerator c43ChildArrangementOrderDocumentParameterGenerator;
    private final C43aSpecialGuardianshipOrderDocumentParameterGenerator
        c43ASpecialGuardianshipOrderDocumentParameterGenerator;
    private final C44aLeaveToChangeTheSurnameOrderParameterGenerator c44aLeaveToChangeTheSurnameOrderParameterGenerator;
    private final C63aDeclarationOfParentageDocumentParameterGenerator
        c63aDeclarationOfParentageDocumentParameterGenerator;
    private final C47AAppointmentOfAChildrensGuardianParameterGenerator c47AParameterGenerator;
    private final C45aParentalResponsibilityOrderDocumentParameterGenerator
        c45aParentalResponsibilityOrderDocumentParameterGenerator;
    private final C36VariationOrExtensionOfSupervisionOrdersParameterGenerator
        c36VariationOrExtensionOfSupervisionOrdersParameterGenerator;
    private final A206PlacementOrderNotificationParameterGenerator a206PlacementOrderNotificationParameterGenerator;
    private final C42FamilyAssistanceOrderDocumentParameterGenerator c42FamilyAssistanceOrderDocumentParameterGenerator;
    private final TransparencyOrderParameterGenerator transparencyOrderParameterGenerator;

    // additional document collectors
    private final C23EPOAdditionalDocumentsCollector c23EPOAdditionalDocumentsCollector;

    private Map<Order, DocmosisParameterGenerator> typeToGenerator;
    private Map<Order, AdditionalDocumentsCollector> typeToAdditionalDocsCollector;
    private Map<Order, DocmosisParameterGenerator> typeToNotificationDocumentGenerator;

    public Map<Order, DocmosisParameterGenerator> getTypeToGenerator() {
        if (typeToGenerator == null) {
            typeToGenerator = List.of(
                a70PlacementOrderDocumentParameterGenerator,
                a81PlacementBlankOrderDocumentParameterGenerator,
                c21BlankOrderDocumentParameterGenerator,
                c23EPODocumentParameterGenerator,
                c26SecureAccommodationOrderDocumentParameterGenerator,
                c29RecoveryOfAChildDocumentParameterGenerator,
                c32CareOrderDocumentParameterGenerator,
                c32bDischargeOfCareOrderDocumentParameterGenerator,
                c33InterimCareOrderDocumentParameterGenerator,
                c35aSupervisionOrderDocumentParameterGenerator,
                c35bISODocumentParameterGenerator,
                c39ChildAssessmentOrderParameterGenerator,
                c37EducationSupervisionOrderParameterGenerator,
                c43ChildArrangementOrderDocumentParameterGenerator,
                c43ASpecialGuardianshipOrderDocumentParameterGenerator,
                c44aLeaveToChangeTheSurnameOrderParameterGenerator,
                c47AParameterGenerator,
                c45aParentalResponsibilityOrderDocumentParameterGenerator,
                c36VariationOrExtensionOfSupervisionOrdersParameterGenerator,
                c34BAuthorityToRefuseContactOrderParameterGenerator,
                c34aContactWithAChildInCareOrderDocumentParameterGenerator,
                c63aDeclarationOfParentageDocumentParameterGenerator,
                c42FamilyAssistanceOrderDocumentParameterGenerator,
                transparencyOrderParameterGenerator
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

    public Optional<DocmosisParameterGenerator> getNotificationDocumentParameterGeneratorByOrderType(Order orderType) {
        return Optional.ofNullable(getTypeToNotificationDocumentGenerator().get(orderType));
    }

    private Map<Order, DocmosisParameterGenerator> getTypeToNotificationDocumentGenerator() {
        if (typeToNotificationDocumentGenerator == null) {
            typeToNotificationDocumentGenerator = Map.of(
                Order.A70_PLACEMENT_ORDER, a206PlacementOrderNotificationParameterGenerator
            );
        }
        return typeToNotificationDocumentGenerator;
    }

}
