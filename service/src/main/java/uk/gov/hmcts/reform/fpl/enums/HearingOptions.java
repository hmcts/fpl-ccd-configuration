package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum HearingOptions {
    @CCD(label = "Add a new hearing")
    NEW_HEARING,
    @CCD(label = "Edit a hearing that has taken place")
    EDIT_PAST_HEARING,
    @CCD(label = "Edit a future hearing")
    EDIT_FUTURE_HEARING,
    @CCD(label = "Adjourn a hearing - the hearing was stopped at the hearing")
    ADJOURN_HEARING,
    @CCD(label = "Vacate a hearing - the hearing was stopped before its listed date")
    VACATE_HEARING,
    @CCD(label = "Re-list an adjourned or vacated hearing")
    RE_LIST_HEARING
}
