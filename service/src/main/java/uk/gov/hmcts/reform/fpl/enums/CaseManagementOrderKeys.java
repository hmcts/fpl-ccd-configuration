package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum CaseManagementOrderKeys {
    SERVED_CASE_MANAGEMENT_ORDERS("servedCaseManagementOrders"),
    CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY("caseManagementOrder"),
    CASE_MANAGEMENT_ORDER_JUDICIARY("cmoToAction"),
    CASE_MANAGEMENT_ORDER_SHARED("sharedDraftCMODocument"),
    ORDER_ACTION("orderAction"),
    SCHEDULE("schedule"),
    RECITALS("recitals"),
    HEARING_DATE_LIST("cmoHearingDateList"),
    NEXT_HEARING_DATE_LIST("nextHearingDateList"),
    DATE_OF_ISSUE("dateOfIssue");

    @Getter
    private final String key;

    CaseManagementOrderKeys(String key) {
        this.key = key;
    }
}
