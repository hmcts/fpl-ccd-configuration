package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListingActionType {
    LISTING_REQUIRED("Listing required"),
    AMEND_VACATE_HEARING("Amend/vacate a hearing"),
    SPECIAL_MEASURES_REQUIRED("Special measures required");

    private final String label;
}
