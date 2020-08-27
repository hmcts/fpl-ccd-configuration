package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.RemoveOrderService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/remove-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoveOrderController {
    private final ObjectMapper mapper;
    private final RemoveOrderService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("removableOrderList", service.buildDynamicListOfOrders(caseData.getOrderCollection()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        UUID id = ElementUtils.getDynamicListValueCode(caseData.getRemovableOrderList(), mapper);

        caseData.getOrderCollection()
            .stream()
            .filter(o -> id.equals(o.getId()))
            .findFirst()
            .ifPresent(orderElement -> {
                GeneratedOrder order = orderElement.getValue();
                data.put("orderToBeRemoved", order.getDocument());
                data.put("orderTitleToBeRemoved", order.getTitle());
                data.put("orderIssuedDateToBeRemoved", order.getDateOfIssue());
                data.put("orderDateToBeRemoved", order.getDate());
            });

        data.put("removableOrderList", service.buildDynamicListOfOrders(caseData.getOrderCollection(), id));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();
        List<Element<GeneratedOrder>> hiddenOrders = caseData.getHiddenOrders();

        service.hideOrder(orders, hiddenOrders, caseData.getRemovableOrderList(), caseData.getReasonToRemoveOrder());

        data.put("orderCollection", orders);
        data.put("hiddenOrders", hiddenOrders);
        removeTemporaryFields(
            caseDetails,
            "removableOrderList",
            "reasonToRemoveOrder",
            "orderToBeRemoved",
            "orderTitleToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved"
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
