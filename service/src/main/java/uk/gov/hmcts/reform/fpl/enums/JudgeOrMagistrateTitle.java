package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum JudgeOrMagistrateTitle {
    HER_HONOUR_JUDGE("Her Honour Judge"),
    HIS_HONOUR_JUDGE("His Honour Judge"),
    DISTRICT_JUDGE("District Judge"),
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),
    DEPUTY_DISTRICT_JUDGE_MAGISTRATES_COURT("District Judge Magistrates Court"),
    MAGISTRATES("Magistrates (JP)"),
    MS_JUSTICE("Ms Justice"),
    LEGAL_ADVISOR("Legal Advisor"),
    MRS_JUSTICE("Mrs Justice"),
    MR_JUSTICE("Mr Justice"),
    OTHER("Other");

    private final String label;

    JudgeOrMagistrateTitle(String label) {
        this.label = label;
    }
}
