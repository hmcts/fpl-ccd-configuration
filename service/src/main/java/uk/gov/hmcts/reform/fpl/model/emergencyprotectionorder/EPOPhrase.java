package uk.gov.hmcts.reform.fpl.model.emergencyprotectionorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class EPOPhrase {
    @CCD(
            label = "Any person who can produce the child[ren] to the applicant must do so",
            typeOverride = FieldType.YesOrNo
    )
    private String includePhrase;
}
