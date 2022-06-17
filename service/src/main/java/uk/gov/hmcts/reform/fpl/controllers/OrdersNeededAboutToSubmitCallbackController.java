package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Api
@RestController
@RequestMapping("/callback/orders-needed")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrdersNeededAboutToSubmitCallbackController extends CallbackController {

    private final HmctsCourtLookupConfiguration courtLookup;

    @PostMapping("/about-to-submit")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        final String showEpoFieldId = "EPO_REASONING_SHOW";
        final CaseData caseData = getCaseData(callbackrequest);
        final String representativeType = Objects.nonNull(caseData.getRepresentativeType()) ?
            caseData.getRepresentativeType().toString() : "LOCAL_AUTHORITY";
        final String ordersText = Objects.equals(representativeType, "LOCAL_AUTHORITY") ? "orders" : "ordersSolicitor";
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get(ordersText))
            .map(orders -> (List<String>) orders.get("orderType"));

        String courtID = Optional.ofNullable((Map<String, Object>) data.get(ordersText))
            .map(orders -> (String) orders.get("court"))
            .map(Object::toString)
            .orElse(null);

        if (orderType.isPresent()) {
            orderType.ifPresent(orderTypes -> {
                if (orderTypes.contains(OrderType.EMERGENCY_PROTECTION_ORDER.name())) {
                    data.put(showEpoFieldId, ImmutableList.of("SHOW_FIELD"));

                } else if (data.containsKey(showEpoFieldId)) {
                    data.remove("groundsForEPO");
                    data.remove(showEpoFieldId);
                }
            });

        } else {
            data.remove("groundsForEPO");
            data.remove(showEpoFieldId);
        }

        if (caseData.isDischargeOfCareApplication()) {
            data.put("otherOrderType", "YES");
        } else {
            data.put("otherOrderType", "NO");
        }

        Court selectedCourt = getCourtSelection(courtID);

        if (Objects.nonNull(selectedCourt)) {
            data.remove("court");
            data.put("court", selectedCourt);
        }

        if (Objects.equals(ordersText, "ordersSolicitor")) {
            data.remove("orders");
            data.put("orders", data.get("ordersSolicitor"));
            data.remove("ordersSolicitor");
        }

        return respond(caseDetails);
    }

    private Court getCourtSelection(String courtID) {
        return courtLookup.getCourtByCode(courtID).orElse(null);
    }
}
