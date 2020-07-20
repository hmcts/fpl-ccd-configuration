package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

/**
 * Errors for the Action CMO event.
 *
 * @deprecated remove once FPLA-1915 goes live
 */
@Getter
@Deprecated(since = "FPLA-1915")
public enum CaseManagementOrderErrorMessages {
    HEARING_NOT_COMPLETED("You can only send this order to parties after the hearing. If the hearing date "
        + "has changed, it needs to be updated.");

    private final String value;

    CaseManagementOrderErrorMessages(String value) {
        this.value = value;
    }
}
