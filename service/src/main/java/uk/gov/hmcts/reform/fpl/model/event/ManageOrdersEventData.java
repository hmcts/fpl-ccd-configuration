package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithEndOfProceedings;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateTypeWithMonth;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class ManageOrdersEventData {
    Order manageOrdersType;
    LocalDate manageOrdersApprovalDate;
    LocalDateTime manageOrdersApprovalDateTime;
    LocalDateTime manageOrdersEndDateTime;
    String manageOrdersFurtherDirections;
    String manageOrdersTitle;
    String manageOrdersDirections;
    EPOType manageOrdersEpoType;
    String manageOrdersIncludePhrase;
    String manageOrdersChildrenDescription;
    String manageOrdersExclusionRequirement;
    String manageOrdersExclusionDetails;
    String manageOrdersWhoIsExcluded;
    DocumentReference manageOrdersPowerOfArrest;
    Address manageOrdersEpoRemovalAddress;
    LocalDate manageOrdersExclusionStartDate;
    ManageOrdersEndDateTypeWithMonth manageOrdersEndDateTypeWithMonth;
    ManageOrdersEndDateTypeWithEndOfProceedings manageOrdersEndDateTypeWithEndOfProceedings;
    LocalDate manageOrdersSetDateEndDate;
    LocalDateTime manageOrdersSetDateAndTimeEndDate;
    Integer manageOrdersSetMonthsEndDate;
}
