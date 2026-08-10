package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CourtServicesNeeded {
    @CCD(label = "Interpreter")
    INTERPRETER,
    @CCD(label = "Intermediary")
    INTERMEDIARY,
    @CCD(label = "Facilities or assistance for a disability")
    FACILITIES_FOR_DISABILITY,
    @CCD(label = "Separate waiting rooms")
    SEPARATE_WAITING_ROOMS,
    @CCD(label = "Something else")
    SOMETHING_ELSE
}
