package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/callback/orders-needed")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrdersNeededController extends CallbackController {

    public static final String ORDERS = "orders";
    public static final List<OrderType> STANDALONE_ORDER_TYPE = List.of(OrderType.CHILD_ASSESSMENT_ORDER,
        OrderType.CONTACT_WITH_CHILD_IN_CARE,
        OrderType.OTHER,
        OrderType.CHILD_RECOVERY_ORDER,
        OrderType.REFUSE_CONTACT_WITH_CHILD,
        OrderType.SECURE_ACCOMMODATION_ORDER,
        OrderType.EDUCATION_SUPERVISION_ORDER);
    public static final List<String> STANDALONE_ORDER_TYPE_NAME = STANDALONE_ORDER_TYPE.stream().map(OrderType::name)
        .collect(Collectors.toList());
    private final HmctsCourtLookupConfiguration courtLookup;
    private final CourtLookUpService courtLookUpService;
    private final DfjAreaLookUpService dfjAreaLookUpService;

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

        if (orderType.isPresent() && orderType.get().size() > 1
            && orderType.get().stream().anyMatch(STANDALONE_ORDER_TYPE_NAME::contains)) {
            return respond(caseDetails, List.of("You have selected a standalone order, "
                + "this cannot be applied for alongside other orders."));
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

                if (!orderTypes.contains(OrderType.CHILD_RECOVERY_ORDER.name())) {
                    data.remove("groundsForChildRecoveryOrder");
                }

                if (!orderTypes.contains(OrderType.EDUCATION_SUPERVISION_ORDER.name())) {
                    data.remove("groundsForEducationSupervisionOrder");
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

        if (caseData.isContactWithChildInCareApplication()) {
            data.put("contactWithChildInCareOrderType", "YES");
        } else {
            data.remove("groundsForContactWithChild");
            data.remove("contactWithChildInCareOrderType");
        }

        if (caseData.isDischargeOfCareApplication()) {
            data.put("otherOrderType", "YES");
        } else {
            data.put("otherOrderType", "NO");
        }

        data.put("c1Application", YesNo.from(caseData.isC1Application()).getValue());

        if (caseData.getOrders() != null) {
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
        }

        String courtID = Optional.ofNullable((Map<String, Object>) data.get(ordersFieldName))
            .map(orders -> (String) orders.get("court"))
            .map(Object::toString)
            .orElse(null);

        Court selectedCourt = getCourtSelection(courtID);

        if (Objects.nonNull(selectedCourt)) {
            data.put("court", selectedCourt);
            DfjAreaCourtMapping dfjArea = dfjAreaLookUpService.getDfjArea(selectedCourt.getCode());
            data.keySet().removeAll(dfjAreaLookUpService.getAllCourtFields());
            data.put("dfjArea", dfjArea.getDfjArea());
            data.put(dfjArea.getCourtField(), selectedCourt.getCode());
        }

        if (ordersFieldName.equals("ordersSolicitor")) {
            data.put("orders", data.get("ordersSolicitor"));
        }

        if (caseData.isC1Application()) {
            data.remove("submittedC1WithSupplement");
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
        /* This needs to get the court out of the NEW list of courts, the old onboarding way might not have all courts
           especially in the lower environments, we should also match the 'Family Court sitting at XYZ' pattern. */
        Optional<Court> court = Optional.ofNullable(courtLookUpService.getCourtByCode(courtID).orElse(null));
        return court.map(c -> c.toBuilder()
            .name("Family Court sitting at " + c.getName())
            .build()).orElse(null);
    }
}
