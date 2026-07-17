package uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class EPOChildren {
    @CCD(label = "Do you want to add a description for the child[ren]?", typeOverride = FieldType.YesOrNo)
    private String descriptionNeeded;
    @CCD(label = "Description", showCondition = "descriptionNeeded=\"Yes\"", typeOverride = FieldType.TextArea)
    private String description;
}
