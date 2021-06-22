package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.EnglandOffices;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.WalesOffices;
import uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ManageOrdersEventData {

    OrderTempQuestions orderTempQuestions;
    Order manageOrdersType;
    DynamicList manageOrdersApprovedAtHearingList;
    State manageOrdersState;
    LocalDate manageOrdersApprovalDate;
    LocalDateTime manageOrdersApprovalDateTime;
    LocalDateTime manageOrdersEndDateTime;
    String manageOrdersFurtherDirections;
    String manageOrdersIsFinalOrder;
    String manageOrdersTitle;
    String manageOrdersDirections;
    EPOType manageOrdersEpoType;
    String manageOrdersIncludePhrase;
    String manageOrdersIsByConsent;
    String manageOrdersChildrenDescription;
    String manageOrdersCareOrderIssuedCourt;
    LocalDate manageOrdersCareOrderIssuedDate;
    String manageOrdersExclusionRequirement;
    String manageOrdersExclusionDetails;
    String manageOrdersWhoIsExcluded;
    DocumentReference manageOrdersPowerOfArrest;
    Address manageOrdersEpoRemovalAddress;
    LocalDate manageOrdersExclusionStartDate;
    ManageOrdersEndDateType manageOrdersEndDateTypeWithMonth;
    ManageOrdersEndDateType manageOrdersEndDateTypeWithEndOfProceedings;
    LocalDate manageOrdersSetDateEndDate;
    LocalDateTime manageOrdersSetDateAndTimeEndDate;
    Integer manageOrdersSetMonthsEndDate;
    String manageOrdersCloseCase;
    String manageOrdersCafcassRegion;
    EnglandOffices manageOrdersCafcassOfficesEngland;
    WalesOffices manageOrdersCafcassOfficesWales;
}
