package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderSourceType;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderNotificationDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.ManageOrdersClosedCaseFieldGenerator;
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.WORD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedOrderHistoryService {

    private final IdentityService identityService;
    private final ChildrenSmartSelector childrenSmartSelector;
    private final AppointedGuardianFormatter appointedGuardianFormatter;
    private final OthersService othersService;
    private final OrderCreationService orderCreationService;
    private final OrderNotificationDocumentService notificationDocumentService;
    private final SealedOrderHistoryExtraTitleGenerator extraTitleGenerator;
    private final SealedOrderHistoryTypeGenerator typeGenerator;
    private final SealedOrderHistoryFinalMarker sealedOrderHistoryFinalMarker;
    private final OthersNotifiedGenerator othersNotifiedGenerator;
    private final SealedOrderLanguageRequirementGenerator languageRequirementGenerator;
    private final Time time;

    private final ManageOrdersClosedCaseFieldGenerator manageOrdersClosedCaseFieldGenerator;

    public Map<String, Object> generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        List<Element<Child>> selectedChildren = childrenSmartSelector.getSelectedChildren(caseData);
        List<Element<Other>> selectedOthers = othersService.getSelectedOthers(caseData);

        DocumentReference order = null;
        DocumentReference plainWordOrder = null;
        // The secure docstore restricted us from accessing documents uploaded by user until the event is submitted.
        // For those documents uploaded by user, all the processing logic (including sealing) are moved to
        // submitted stage.
        if (OrderSourceType.MANUAL_UPLOAD != manageOrdersEventData.getManageOrdersType().getSourceType()) {
            order = orderCreationService.createOrderDocument(caseData, OrderStatus.SEALED, PDF);
            plainWordOrder = orderCreationService.createOrderDocument(caseData, OrderStatus.PLAIN, WORD);
        }

        GeneratedOrder.GeneratedOrderBuilder generatedOrderBuilder = GeneratedOrder.builder()
            .orderType(manageOrdersEventData.getManageOrdersType().name()) // hidden field, to store the type
            .title(extraTitleGenerator.generate(caseData))
            .type(typeGenerator.generate(caseData))
            .markedFinal(sealedOrderHistoryFinalMarker.calculate(caseData).getValue())
            .children(selectedChildren)
            .others(selectedOthers) // hidden field, to store the selected others for notify
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .dateTimeIssued(time.now())
            .approvalDate(manageOrdersEventData.getManageOrdersApprovalDate())
            .approvalDateTime(manageOrdersEventData.getManageOrdersApprovalDateTime())
            .childrenDescription(getChildrenForOrder(selectedChildren, caseData))
            .specialGuardians(appointedGuardianFormatter.getGuardiansNamesForTab(caseData))
            .othersNotified(othersNotifiedGenerator.getOthersNotified(selectedOthers))
            .document(order)
            .translationRequirements(languageRequirementGenerator.translationRequirements(caseData))
            .unsealedDocumentCopy(plainWordOrder);

        notificationDocumentService.createNotificationDocument(caseData)
            .ifPresent(generatedOrderBuilder::notificationDocument);

        Optional.ofNullable(manageOrdersEventData.getManageOrdersLinkedApplication())
            .map(DynamicList::getValueCode)
            .ifPresent(generatedOrderBuilder::linkedApplicationId);

        List<Element<GeneratedOrder>> pastOrders = caseData.getOrderCollection();

        pastOrders.add(element(identityService.generateId(), generatedOrderBuilder.build()));

        pastOrders.sort(legacyLastAndThenByApprovalDateAndIssuedDateTimeDesc());

        Map<String, Object> data = new HashMap<>(manageOrdersClosedCaseFieldGenerator.generate(caseData));
        data.put("orderCollection", pastOrders);
        return data;
    }

    public GeneratedOrder lastGeneratedOrder(CaseData caseData) {
        return lastGeneratedOrderElement(caseData).getValue();
    }

    private Element<GeneratedOrder> lastGeneratedOrderElement(CaseData caseData) {
        return caseData.getOrderCollection().stream()
            .min(legacyLastAndThenByDateAndTimeIssuedDesc())
            .orElseThrow(() -> new IllegalStateException("Element not present"));
    }

    public Map<String, Object> processUploadedOrder(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        // get the latest order just created in about-to-submit callback;
        Element<GeneratedOrder> orderElm = lastGeneratedOrderElement(caseData);
        GeneratedOrder order = orderElm.getValue();

        DocumentReference sealedPDForder = orderCreationService.createOrderDocument(caseData, OrderStatus.SEALED, PDF);
        DocumentReference plainWordOrder = orderCreationService.createOrderDocument(caseData, OrderStatus.PLAIN, WORD);

        GeneratedOrder sealedOrder = order.toBuilder()
            .document(sealedPDForder)
            .unsealedDocumentCopy(plainWordOrder)
            .build();

        List<Element<GeneratedOrder>> pastOrders = caseData.getOrderCollection();
        pastOrders.set(pastOrders.indexOf(orderElm), element(orderElm.getId(), sealedOrder));
        data.put("orderCollection", pastOrders);
        return data;
    }

    private Comparator<Element<GeneratedOrder>> legacyLastAndThenByApprovalDateAndIssuedDateTimeDesc() {
        Comparator<Element<GeneratedOrder>> comparingApprovalDate = comparing(
            this::getOrderApprovalDateTime, nullsLast(reverseOrder())
        );
        return comparingApprovalDate.thenComparing(
            e -> e.getValue().getDateTimeIssued(), nullsLast(reverseOrder())
        );
    }

    private LocalDateTime getOrderApprovalDateTime(Element<GeneratedOrder> orderElement) {
        if (isNull(orderElement.getValue().getApprovalDateTime())) {
            if (!isNull(orderElement.getValue().getApprovalDate())) {
                return orderElement.getValue().getApprovalDate().atStartOfDay();
            }
        }

        return orderElement.getValue().getApprovalDateTime();
    }

    private Comparator<Element<GeneratedOrder>> legacyLastAndThenByDateAndTimeIssuedDesc() {
        return comparing(e -> e.getValue().getDateTimeIssued(),
            Comparator.nullsLast(reverseOrder()));
    }

    private String getChildrenForOrder(List<Element<Child>> selectedChildren, CaseData caseData) {
        String appliesToAllChildren = caseData.getOrderAppliesToAllChildren();

        if (YES.getValue().equals(appliesToAllChildren)) {
            return null;
        }

        return Optional.ofNullable(selectedChildren).map(
            children -> children.stream().map(
                child -> child.getValue().asLabel()
            ).collect(Collectors.joining(", "))
        ).orElse(null);
    }
}

