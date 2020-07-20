package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

/**
 * CCD field names for CMO.
 *
 * @deprecated remove once FPLA-1915 goes live
 */
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
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
