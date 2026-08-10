package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FactorsAffectingParentingList", generate = true)
public enum FactorsAffectingParentingType {
    @CCD(label = "Alcohol or drug abuse")
    ALCOHOL_DRUG_ABUSE,
    @CCD(label = "Domestic abuse")
    DOMESTIC_ABUSE,
    @CCD(label = "Anything else")
    ANYTHING_ELSE
}
