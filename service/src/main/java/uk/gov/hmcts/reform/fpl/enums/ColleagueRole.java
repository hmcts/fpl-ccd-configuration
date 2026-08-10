package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ColleagueRole {
    @CCD(label = "Solicitor")
    SOLICITOR("Solicitor"),
    @CCD(label = "Social worker")
    SOCIAL_WORKER("Social worker"),
    @CCD(label = "Other colleague")
    OTHER("Other");

    private final String label;
}
