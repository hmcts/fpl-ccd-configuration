package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;


@ComplexType(name = "Applicants", generate = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Applicant {
    @CCD(label = "Party")
    @Valid
    @NotNull(message = "You need to add details to applicant")
    private final ApplicantParty party;
    @CCD(
            label = "Is this the lead applicant?",
            showCondition = "party=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String leadApplicantIndicator;
}
