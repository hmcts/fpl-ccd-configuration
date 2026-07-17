package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class FurtherDirections {
    @CCD(label = "Do you want to add further directions to the order?", typeOverride = FieldType.YesOrNo)
    private final String directionsNeeded;
    @CCD(
            label = "Enter directions",
            hint = "These will be printed at the bottom of the order.",
            showCondition = "directionsNeeded = \"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String directions;
}
