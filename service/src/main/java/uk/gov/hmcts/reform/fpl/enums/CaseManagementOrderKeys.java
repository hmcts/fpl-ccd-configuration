package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum CaseManagementOrderKeys {
    CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY("caseManagementOrder_LocalAuthority"),
    CASE_MANAGEMENT_ORDER_JUDICIARY("caseManagementOrder_Judiciary"),
    CASE_MANAGEMENT_ORDER_SHARED("sharedDraftCMODocument"),
    ORDER_ACTION("orderAction"),
    SCHEDULE("schedule"),
    RECITALS("recitals"),
    HEARING_DATE_LIST("cmoHearingDateList");

    @Getter
    private final String key;

    CaseManagementOrderKeys(String key) {
        this.key = key;
    }
}
