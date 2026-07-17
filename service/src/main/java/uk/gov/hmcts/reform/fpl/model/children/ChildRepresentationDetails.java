package uk.gov.hmcts.reform.fpl.model.children;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Value
@Builder
@Jacksonized
public class ChildRepresentationDetails {
    @CCD(label = " ")
    String childDescription;
    @CCD(label = "Is this child using the Cafcass representative?", typeOverride = FieldType.YesOrNo)
    String useMainSolicitor;
    @CCD(label = " ", showCondition = "useMainSolicitor = \"No\"")
    RespondentSolicitor solicitor;
}
