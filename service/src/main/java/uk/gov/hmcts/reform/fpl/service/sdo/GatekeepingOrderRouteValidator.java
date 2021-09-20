package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;

@Component
public class GatekeepingOrderRouteValidator {

    private static final String URGENT_ROUTE_VALIDATION_MESSAGE = "An urgent hearing order has already been added to"
                                                                  + " this case. You can still add a gatekeeping "
                                                                  + "order, if needed.";
    private static final String EVENT_ACCESS_VALIDATION_MESSAGE = "There is already a gatekeeping order for this case";
    private static final String HEARING_DETAILS_REQUIRED = "You need to add hearing details for the notice of "
                                                           + "proceedings";

    public List<String> allowAccessToEvent(CaseData caseData) {
        StandardDirectionOrder sdo = defaultIfNull(
            caseData.getStandardDirectionOrder(), StandardDirectionOrder.builder().build()
        );

        return sdo.isSealed() ? List.of(EVENT_ACCESS_VALIDATION_MESSAGE) : List.of();
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
}
