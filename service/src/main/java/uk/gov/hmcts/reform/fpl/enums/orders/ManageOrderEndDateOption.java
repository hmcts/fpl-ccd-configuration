package uk.gov.hmcts.reform.fpl.enums.orders;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ManageOrderEndDateTypeWithEducationAge", generate = true)
public enum ManageOrderEndDateOption {
    @CCD(label = "12 months from date of order")
    TWELVE_MONTHS_FROM_DATE_OF_ORDER,
    @CCD(label = "Until end of compulsory education age")
    UNTIL_END_OF_COMPULSORY_EDUCATION_AGE
}
