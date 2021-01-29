package uk.gov.hmcts.reform.fpl.enums;

public enum HearingOrderType {
    DRAFT_CMO, AGREED_CMO, C21;

    public boolean isCmo() {
        return this == DRAFT_CMO || this == AGREED_CMO;
    }
}
