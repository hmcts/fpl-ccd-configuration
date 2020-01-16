package uk.gov.hmcts.reform.fpl.enums.ccd.casefields;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseManagementOrderKey implements CaseField {
    SERVED_CASE_MANAGEMENT_ORDERS("servedCaseManagementOrders"),
    CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY("caseManagementOrder"),
    CASE_MANAGEMENT_ORDER_JUDICIARY("cmoToAction"),
    CASE_MANAGEMENT_ORDER_SHARED("sharedDraftCMODocument"),
    ORDER_ACTION("orderAction"),
    SCHEDULE("schedule"),
    RECITALS("recitals"),
    HEARING_DATE_LIST("cmoHearingDateList"),
    NEXT_HEARING_DATE_LIST("nextHearingDateList");

    private final String key;
}
