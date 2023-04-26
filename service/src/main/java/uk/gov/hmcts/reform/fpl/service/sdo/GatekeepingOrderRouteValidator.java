package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ADD_URGENT_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;

@Component
public class GatekeepingOrderRouteValidator {

    protected static final String URGENT_DIRECTIONS_VALIDATION_MESSAGE = "An urgent directions order has already been"
        + " added to this case.";
    private static final String URGENT_ROUTE_VALIDATION_MESSAGE = "An urgent hearing order has already been added to"
        + " this case. You can still add a gatekeeping "
        + "order, if needed.";
    private static final String NO_URGENT_DIRECTIONS_REQUIRED_MESSAGE = "An urgent directions order is not"
        + " required for this case.";
    private static final String EVENT_ACCESS_VALIDATION_MESSAGE = "There is already a gatekeeping order for this case";
    private static final String HEARING_DETAILS_REQUIRED = "You need to add hearing details for the notice of "
        + "proceedings";
    private static final String URGENT_DIRECTIONS_NOT_ALLOWED_MESSAGE =
        "There is a standard direction order on this case and the urgent direction event can not be used.";

    public List<String> allowAccessToEvent(CaseData caseData) {
        return isStandDirectionExist(caseData) ? List.of(EVENT_ACCESS_VALIDATION_MESSAGE) : List.of();
    }

    public List<String> allowAccessToEvent(CaseData caseData, String eventName) {
        if (eventName.equals(ADD_URGENT_DIRECTIONS.getId())) {
            if (allowAddUrgentDirections(caseData)) {
                StandardDirectionOrder udo = defaultIfNull(caseData.getUrgentDirectionsOrder(),
                    StandardDirectionOrder.builder().build());
                if (udo.isSealed()) {
                    return List.of(URGENT_DIRECTIONS_VALIDATION_MESSAGE);
                } else if (isStandDirectionExist(caseData)) {
                    return List.of(URGENT_DIRECTIONS_NOT_ALLOWED_MESSAGE);
                } else {
                    return List.of();
                }
            } else {
                return List.of(NO_URGENT_DIRECTIONS_REQUIRED_MESSAGE);
            }
        } else {
            return allowAccessToEvent(caseData);
        }
    }

    public List<String> allowAccessToUrgentHearingRoute(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (CASE_MANAGEMENT == caseData.getState()) {
            errors.add(URGENT_ROUTE_VALIDATION_MESSAGE);
        } else if (isEmpty(caseData.getHearingDetails())) {
            errors.add(HEARING_DETAILS_REQUIRED);
        }
        return errors;
    }

    private boolean allowAddUrgentDirections(CaseData caseData) {
        return caseData.isCareOrderCombinedWithUrgentDirections() || caseData.isStandaloneEPOApplication()
            || caseData.isStandaloneInterimCareOrder() || caseData.isStandaloneSecureAccommodationOrder()
            || caseData.isStandaloneChildRecoveryOrder() || caseData.isEPOCombinedWithICO();
    }

    private boolean isStandDirectionExist(CaseData caseData) {
        StandardDirectionOrder sdo = defaultIfNull(
            caseData.getStandardDirectionOrder(), StandardDirectionOrder.builder().build()
        );

        return sdo.isSealed();
    }
}
