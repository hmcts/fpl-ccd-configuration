package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/orders-needed")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrdersNeededAboutToSubmitCallbackController extends CallbackController {

    public static final String ORDERS = "orders";

    private final CourtLookUpService courtLookUpService;

    @PostMapping("/mid-event")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get("orders"))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (orderType.isPresent()
            && orderType.get().contains(OrderType.CHILD_ASSESSMENT_ORDER.name()) && orderType.get().size() > 1) {
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
            });

        } else {
            data.remove("groundsForEPO");
            data.remove(showEpoFieldId);
            removeSecureAccommodationOrderFields(data);
        }

        if (caseData.isDischargeOfCareApplication()) {
            data.put("otherOrderType", "YES");
        } else {
            data.put("otherOrderType", "NO");
        }

        String courtCode = caseData.getOrders().getCourt();
        Optional<Court> lookedUpCourt = courtLookUpService.getCourtByCode(courtCode);
        if (lookedUpCourt.isPresent()) {
            data.put("caseManagementLocation", CaseLocation.builder()
                .baseLocation(lookedUpCourt.get().getEpimmsId())
                .region(lookedUpCourt.get().getRegionId())
                .build());
        } else {
            log.error("Fail to lookup ePIMMS ID for code: " + courtCode);
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
