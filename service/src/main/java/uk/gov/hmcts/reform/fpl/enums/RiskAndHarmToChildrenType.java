package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RiskAndHarmToChildrenList", generate = true)
public enum RiskAndHarmToChildrenType {
    @CCD(label = "Physical harm including non-accidental injury")
    PHYSICAL_HARM,
    @CCD(label = "Emotional harm")
    EMOTIONAL_HARM,
    @CCD(label = "Sexual abuse")
    SEXUAL_ABUSE,
    @CCD(label = "Neglect")
    NEGLECT    
}
