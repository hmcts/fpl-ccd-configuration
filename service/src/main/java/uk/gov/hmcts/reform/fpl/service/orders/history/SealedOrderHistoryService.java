package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.orders.OrderCreationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.PDF;
import static uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat.WORD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedOrderHistoryService {

    private final IdentityService identityService;
    private final ChildrenService childrenService;
    private final OrderCreationService orderCreationService;
    private final Time time;

    public Map<String, Object> generate(CaseData caseData) {
        List<Element<GeneratedOrder>> pastOrders = caseData.getOrderCollection();
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        DocumentReference sealedPdfOrder = orderCreationService.createOrderDocument(caseData, OrderStatus.SEALED, PDF);
        DocumentReference plainWordOrder = orderCreationService.createOrderDocument(caseData, OrderStatus.PLAIN, WORD);

        pastOrders.add(element(identityService.generateId(), GeneratedOrder.builder()
            .orderType(CLOSED == caseData.getState() ? Order.C21_BLANK_ORDER.name()
                : manageOrdersEventData.getManageOrdersType().name()) // hidden field, to store the type
            .title(manageOrdersEventData.getManageOrdersTitle())
            .type(CLOSED == caseData.getState() ? Order.C21_BLANK_ORDER.getHistoryTitle()
                : manageOrdersEventData.getManageOrdersType().getHistoryTitle())
            .children(selectedChildren)
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .dateTimeIssued(time.now())
            .approvalDate(manageOrdersEventData.getManageOrdersApprovalDate())
            .approvalDateTime(manageOrdersEventData.getManageOrdersApprovalDateTime())
            .childrenDescription(getChildrenForOrder(selectedChildren))
            .document(sealedPdfOrder)
            .unsealedDocumentCopy(plainWordOrder)
            .build()));

        pastOrders.sort(legacyLastAndThenByApprovalDateAndIssuedDateTimeDesc());

        return Map.of("orderCollection", pastOrders);
    }

    public GeneratedOrder lastGeneratedOrder(CaseData caseData) {
        return caseData.getOrderCollection().stream()
            .min(legacyLastAndThenByDateAndTimeIssuedDesc())
            .orElseThrow(() -> new IllegalStateException("Element not present"))
            .getValue();
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

    private String getChildrenForOrder(List<Element<Child>> selectedChildren) {
        return Optional.ofNullable(selectedChildren).map(
            children -> children.stream().map(
                child -> child.getValue().asLabel()
            ).collect(Collectors.joining(", "))
        ).orElse(null);
    }
}

