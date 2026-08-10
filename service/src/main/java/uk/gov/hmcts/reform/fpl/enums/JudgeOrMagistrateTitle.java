package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum JudgeOrMagistrateTitle {
    @CCD(label = "Her Honour Judge")
    HER_HONOUR_JUDGE("Her Honour Judge"),
    @CCD(label = "His Honour Judge")
    HIS_HONOUR_JUDGE("His Honour Judge"),
    @CCD(label = "Circuit Judge (sitting in retirement)")
    CIRCUIT_JUDGE_SITTING_IN_RETIRE("Circuit Judge (sitting in retirement)"),
    @CCD(label = "District Judge (sitting in retirement)")
    DISTRICT_JUDGE_SITTING_IN_RETIRE("District Judge (sitting in retirement)"),
    @CCD(label = "District Judge")
    DISTRICT_JUDGE("District Judge"),
    @CCD(label = "Deputy District Judge")
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),
    @CCD(label = "District Judge Magistrates Court")
    DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT("District Judge Magistrates Court"),
    @CCD(label = "Deputy High Court Judge")
    DEPUTY_HIGH_COURT_JUDGE("Deputy High Court Judge"),
    @CCD(label = "Magistrates (JP)")
    MAGISTRATES("Magistrates (JP)"),
    @CCD(label = "Ms Justice")
    MS_JUSTICE("Ms Justice"),
    @CCD(label = "Legal Adviser")
    LEGAL_ADVISOR("Legal Adviser"),
    @CCD(label = "Mrs Justice")
    MRS_JUSTICE("Mrs Justice"),
    @CCD(label = "Mr Justice")
    MR_JUSTICE("Mr Justice"),
    @CCD(label = "Recorder")
    RECORDER("Recorder"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String label;

    JudgeOrMagistrateTitle(String label) {
        this.label = label;
    }
}
