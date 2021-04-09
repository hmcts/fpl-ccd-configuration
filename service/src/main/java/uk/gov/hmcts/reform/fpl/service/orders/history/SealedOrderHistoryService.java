package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getJudgeForTabView;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SealedOrderHistoryService {

    private final IdentityService identityService;
    private final ChildrenService childrenService;
    private final Time time;

    public Map<String, Object> generate(CaseData caseData) {
        List<Element<GeneratedOrder>> pastOrders = caseData.getOrderCollection();
        ManageOrdersEventData manageOrdersEventData =  caseData.getManageOrdersEventData();
        List<Element<Child>> selectedChildren = childrenService.getSelectedChildren(caseData);

        pastOrders.add(element(identityService.generateId(), GeneratedOrder.builder()
            .orderType(manageOrdersEventData.getManageOrdersType().name()) // hidden field, to store the type
            .title(manageOrdersEventData.getManageOrdersType().getHistoryTitle())
            .children(selectedChildren)
            .judgeAndLegalAdvisor(getJudgeForTabView(caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()))
            .dateIssued(time.now().toLocalDate())
            .approvalDate(manageOrdersEventData.getManageOrdersApprovalDate())
            .childrenDescription(getChildrenForOrder(selectedChildren))
            .build()));

        pastOrders.sort(legacyFirstAndThenByApprovalDateAsc());

        return Map.of("orderCollection", pastOrders);
    }

    private Comparator<Element<GeneratedOrder>> legacyFirstAndThenByApprovalDateAsc() {
        return Comparator.comparing(e -> e.getValue().getApprovalDate(),
            Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private String getChildrenForOrder(List<Element<Child>> selectedChildren) {
        return Optional.ofNullable(selectedChildren).map(
            children -> children.stream().map(
                child -> child.getValue().asLabel()
            ).collect(Collectors.joining(", "))
        ).orElse(null);
    }

}

