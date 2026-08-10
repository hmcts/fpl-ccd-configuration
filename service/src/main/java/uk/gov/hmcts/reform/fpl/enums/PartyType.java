package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum PartyType {
    @CCD(label = "Individual")
    INDIVIDUAL,
    @CCD(label = "Organisation")
    ORGANISATION
}
