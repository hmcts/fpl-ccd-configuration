package uk.gov.hmcts.reform.fpl.enums;

/**
 * Next hearing type after the sealed CMO.
 *
 * @deprecated to be removed with {@link uk.gov.hmcts.reform.fpl.model.CaseManagementOrder}
 */
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public enum NextHearingType {
    FURTHER_CASE_MGMT_HEARING,
    ISSUES_RESOLUTION_HEARING,
    FINAL_HEARING
}
