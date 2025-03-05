package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllocationProposaJudgeTypes {
    CIRCUIT_JUDGE("Circuit judge", "Circuit judge"),
    @Deprecated
    CIRCUIT_JUDGE_SECTION_9("Section 9 circuit judge", "Circuit Judge (Section 9)"),
    DISTRICT_JUDGE("District Judge", "District Judge"),
    MAGISTRATE("Lay justices", "Magistrate"),
    HIGH_COURT_JUDGE("High Court judge", "High Court Judge");

    private final String value;
    private final String label;


}
