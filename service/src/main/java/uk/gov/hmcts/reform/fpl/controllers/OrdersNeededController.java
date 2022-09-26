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
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CONTACT_WITH_CHILD_IN_CARE;

@Slf4j
@Api
@RestController
@RequestMapping("/callback/orders-needed")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrdersNeededController extends CallbackController {

    private final HmctsCourtLookupConfiguration courtLookup;

    @PostMapping("/about-to-start")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartEvent(
        @RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        if (Objects.isNull(caseData.getRepresentativeType())) {
            data.put("representativeType", RepresentativeType.LOCAL_AUTHORITY);
        }

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final RepresentativeType representativeType = Objects.nonNull(caseData.getRepresentativeType())
            ? caseData.getRepresentativeType() : RepresentativeType.LOCAL_AUTHORITY;
        final String ordersFieldName = representativeType.equals(RepresentativeType.LOCAL_AUTHORITY)
            ? "orders" : "ordersSolicitor";
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get(ordersFieldName))
            .map(orders -> (List<String>) orders.get("orderType"));

        if (orderType.isPresent() && orderType.get().size() > 1) {
            if (orderType.get().contains(OrderType.CHILD_ASSESSMENT_ORDER.name())
                || orderType.get().contains(OrderType.CONTACT_WITH_CHILD_IN_CARE.name())
                || orderType.get().contains(OrderType.OTHER.name())) {
                return respond(caseDetails, List.of("You have selected a standalone order, "
                    + "this cannot be applied for alongside other orders."));
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackrequest) {
        final String showEpoFieldId = "EPO_REASONING_SHOW";
        final CaseData caseData = getCaseData(callbackrequest);
        final RepresentativeType representativeType = Objects.nonNull(caseData.getRepresentativeType())
            ? caseData.getRepresentativeType() : RepresentativeType.LOCAL_AUTHORITY;
        final String ordersFieldName = representativeType.equals(RepresentativeType.LOCAL_AUTHORITY)
            ? "orders" : "ordersSolicitor";
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();

        Optional<List<String>> orderType = Optional.ofNullable((Map<String, Object>) data.get(ordersFieldName))
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
                    removeSecureAccommodationOrderFields(data, ordersFieldName);
                } else {
                    data.put("secureAccommodationOrderType", YesNo.YES);
                }
            });

        } else {
            data.remove("groundsForEPO");
            data.remove(showEpoFieldId);
            removeSecureAccommodationOrderFields(data, ordersFieldName);
        }

        if (caseData.isRefuseContactWithChildApplication()) {
            data.put("refuseContactWithChildOrderType", YesNo.YES);
        } else {
            data.remove("groundsForRefuseContactWithChild");
            data.remove("refuseContactWithChildOrderType");
        }

        if (isContactWithChildInCareOrder(orderType)) {
            data.put("contactWithChildInCareOrderType", "YES");
            log.info("CIC");
        } else {
            log.info("NON CIC");
            data.remove("groundsForContactWithChildInCare");
            data.remove("contactWithChildInCareOrderType");
        }

        if (caseData.isDischargeOfCareApplication()) {
            data.put("otherOrderType", "YES");
            log.info("DOC");
        } else {
            log.info("NON DOC");
            data.put("otherOrderType", "NO");
        }

        String courtID = Optional.ofNullable((Map<String, Object>) data.get(ordersFieldName))
            .map(orders -> (String) orders.get("court"))
            .map(Object::toString)
            .orElse(null);

        Court selectedCourt = getCourtSelection(courtID);

        if (Objects.nonNull(selectedCourt)) {
            data.put("court", selectedCourt);
        }

        if (ordersFieldName.equals("ordersSolicitor")) {
            data.put("orders", data.get("ordersSolicitor"));
        }

        return respond(caseDetails);
    }

    @SuppressWarnings("unchecked")
    private void removeSecureAccommodationOrderFields(Map<String, Object> data, String ordersFieldName) {
        data.remove("groundsForSecureAccommodationOrder");
        // remove the secureAccommodationOrderSection field
        ((Map<String, Object>) data.get(ordersFieldName)).remove("secureAccommodationOrderSection");

        // set this control flag to NO
        data.put("secureAccommodationOrderType", YesNo.NO);
    }

    private Court getCourtSelection(String courtID) {
        return courtLookup.getCourtByCode(courtID).orElse(null);
    }

    private boolean isContactWithChildInCareOrder(Optional<List<String>> orderType) {
        return orderType.isPresent()
            && orderType.get().size() == 1
            && orderType.get().contains(OrderType.CONTACT_WITH_CHILD_IN_CARE.name());
    }

    private boolean isDischargeOfCareOrder(Optional<List<String>> orderType) {
        return orderType.isPresent()
            && orderType.get().size() == 1
            && orderType.get().contains(OrderType.OTHER.name());
    }
}
