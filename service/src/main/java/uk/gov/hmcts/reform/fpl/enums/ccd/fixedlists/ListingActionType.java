package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ListingActionType {
    @CCD(label = "Listing required")
    LISTING_REQUIRED("Listing required"),
    @CCD(label = "Amend/vacate a hearing")
    AMEND_VACATE_HEARING("Amend/vacate a hearing"),
    @CCD(label = "Special measures required")
    SPECIAL_MEASURES_REQUIRED("Special measures required");

    private final String label;
}
