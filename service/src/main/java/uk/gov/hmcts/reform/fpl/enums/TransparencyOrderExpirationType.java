package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum TransparencyOrderExpirationType {
  @CCD(label = "Date to be chosen")
  DATE_TO_BE_CHOSEN,
  @CCD(label = "The 18th birthday of the youngest child")
  THE_18TH_BDAY_YOUNGEST_CHILD
}
