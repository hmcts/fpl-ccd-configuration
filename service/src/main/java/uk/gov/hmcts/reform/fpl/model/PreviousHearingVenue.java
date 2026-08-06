package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class PreviousHearingVenue {
    @CCD(label = "Last court")
    private final String previousVenue;
    @CCD(label = "Use this court for this hearing?", typeOverride = FieldType.YesOrNo)
    private final String usePreviousVenue;
    @CCD(
            label = "Court",
            showCondition = "usePreviousVenue=\"No\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingVenue"
    )
    private final String newVenue;
    @CCD(
            label = "Court address",
            showCondition = "usePreviousVenue=\"No\" AND newVenue=\"OTHER\"",
            typeOverride = FieldType.AddressUK
    )
    private final Address newVenueCustomAddress;
}
