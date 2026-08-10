package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum JudgeType {
    @CCD(label = "Salaried judge")
    SALARIED_JUDGE,
    @CCD(label = "Fee paid judge")
    FEE_PAID_JUDGE,
    @CCD(label = "Legal adviser")
    LEGAL_ADVISOR
}
