package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/callback/orders-needed")
public class OrdersNeededAboutToSubmitCallbackController extends CallbackController {

    public static final String ORDERS = "orders";
    public static final List<OrderType> STANDALONE_ORDER_TYPE = List.of(OrderType.CHILD_ASSESSMENT_ORDER,
        OrderType.CHILD_RECOVERY_ORDER);
    public static final List<String> STANDALONE_ORDER_TYPE_NAME = STANDALONE_ORDER_TYPE.stream().map(OrderType::name)
        .collect(Collectors.toList());

    @PostMapping("/mid-event")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get("orders"))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (orderType.isPresent() && orderType.get().size() > 1
            && orderType.get().stream().anyMatch(STANDALONE_ORDER_TYPE_NAME::contains)) {
            return respond(caseDetails, List.of("You have selected a standalone order, "
                + "this cannot be applied for alongside other orders."));
        }
        return respond(caseDetails);
    }


    @PostMapping("/about-to-submit")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        final String showEpoFieldId = "EPO_REASONING_SHOW";
        final CaseData caseData = getCaseData(callbackrequest);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get(ORDERS))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (orderType.isPresent()) {
            orderType.ifPresent(orderTypes -> {
                if (orderTypes.contains(OrderType.EMERGENCY_PROTECTION_ORDER.name())) {
                    data.put(showEpoFieldId, ImmutableList.of("SHOW_FIELD"));

                } else if (data.containsKey(showEpoFieldId)) {
                    data.remove("groundsForEPO");
                    data.remove(showEpoFieldId);
                }
                if (!orderTypes.contains(OrderType.SECURE_ACCOMMODATION_ORDER.name())) {
                    removeSecureAccommodationOrderFields(data);
                } else {
                    data.put("secureAccommodationOrderType", YesNo.YES);
                }

                if (!orderTypes.contains(OrderType.CHILD_RECOVERY_ORDER.name())) {
                    data.remove("groundsForChildRecoveryOrder");
                }
            });

        } else {
            data.remove("groundsForEPO");
            data.remove(showEpoFieldId);
            removeSecureAccommodationOrderFields(data);
        }

        if (caseData.isRefuseContactWithChildApplication()) {
            data.put("refuseContactWithChildOrderType", YesNo.YES);
        } else {
            data.remove("groundsForRefuseContactWithChild");
            data.remove("refuseContactWithChildOrderType");
        }

        if (caseData.isDischargeOfCareApplication()) {
            data.put("otherOrderType", "YES");
        } else {
            data.put("otherOrderType", "NO");
        }

        return respond(caseDetails);
    }

    @SuppressWarnings("unchecked")
    private void removeSecureAccommodationOrderFields(Map<String, Object> data) {
        data.remove("groundsForSecureAccommodationOrder");
        // remove the secureAccommodationOrderSection field
        ((Map<String, Object>) data.get(ORDERS)).remove("secureAccommodationOrderSection");

        // set this control flag to NO
        data.put("secureAccommodationOrderType", YesNo.NO);
    }
}
